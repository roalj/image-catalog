package beans;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.logs.cdi.Log;
import config.IntegrationProperties;
import entities.ImageEntity;
import io.smallrye.faulttolerance.config.CircuitBreakerConfig;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Log
@RequestScoped
public class ImageBean {
    private Logger log = Logger.getLogger(ImageBean.class.getName());

    @PersistenceContext(unitName = "images-jpa")
    private EntityManager em;

    private Client httpClient;

    @Inject
    @DiscoverService(value = "comments-service", environment = "dev", version = "1.0.0")
    private Optional<String> baseUrl;

    @Inject
    private ImageBean imageBeanProxy;

    @Inject
    private IntegrationProperties integrationProperties;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
    }

    public List getImageList(){
        Query query = em.createNamedQuery("Image.getAll", ImageEntity.class);
        return query.getResultList();
    }

    public ImageEntity getImage(Integer id) {
        ImageEntity imageEntity = em.find(ImageEntity.class, id);

        if (imageEntity == null) {
            throw  new NotFoundException();
        }

        if (integrationProperties.isIntegrateWithCommentsService()) {
            imageEntity.setCommentsCount(imageBeanProxy.getCommentCount(id));
        }

        return imageEntity;
    }

    public String getImageUrl(Integer id) {
        ImageEntity imageEntity = em.find(ImageEntity.class, id);

        if (imageEntity == null) {
            throw  new NotFoundException();
        }

        return imageEntity.getMongoId();
    }

    //CircuitBreaker @Fallback doesnt' invoke circuit.breaker.prevented metrics
    //http://henszey.github.io/etcd-browser/ -> nastimaj test-error na true -> klici metodo 3-krat -> open state
    // nastimaj test-error na false, počakaj par sekund, in ga nastimaj na true -> v tem casu je v half_open -> klici in prvi klic bo internal server error, drugi bo že prekinjen
    @Timed(name = "CircuitBreakerTimer")
   // @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 3)
    //@Fallback(fallbackMethod = "getCommentCountFallback")
    public Integer getCommentCount(Integer imageId) {
        if (baseUrl.isPresent()) {
            log.info("Calling comments service: getting comment count. " + baseUrl);
            try {
                return httpClient
                        .target(baseUrl.get() + "/api/comments/count")
                        .queryParam("imageId", imageId)
                        .request().get(new GenericType<Integer>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.severe(e.getMessage());
                throw new InternalServerErrorException(e);
            }
        }
        return null;
    }

    public Integer getCommentCountFallback(Integer imageId) {
        return 12;
    }

    public boolean deleteImage(Integer imageId) {
        ImageEntity image = em.find(ImageEntity.class, imageId);

        if (image != null) {
            try {
                beginTx();
                em.remove(image);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else
            return false;

        return true;
    }

    public ImageEntity createImage(ImageEntity image) {
        try {
            beginTx();
            em.persist(image);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return image;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    private void commitTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().commit();
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();
    }
}

package beans;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import entities.ImageEntity;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;

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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


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

        imageEntity.setCommentsCount(imageBeanProxy.getCommentCount(id));

        return imageEntity;
    }

    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 3)
    @Fallback(fallbackMethod = "getCommentCountFallback")
    private Integer getCommentCount(Integer imageId) {
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

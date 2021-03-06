package resource;

import beans.ImageBean;
import client.ImageAnalysingApi;
import com.kumuluz.ee.logs.cdi.Log;
import entities.ImageEntity;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/images")
@Log
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ImageResource {

    private Logger log = Logger.getLogger(ImageResource.class.getName());

    @Inject
    private ImageBean imageBean;

    @Inject
    @RestClient
    private ImageAnalysingApi imageAnalysingApi;

    @GET
    @Timed(name = "getMethod_timer")
    public Response getImageList() {
        final List<entities.ImageEntity> imageList = imageBean.getImageList();
        return Response.ok(imageList).build();
    }

    @GET
    @Path("/mongoId/{imageId}")
    public Response getImageUrl(@PathParam("imageId") Integer imageId) {
        return Response.status(Response.Status.OK).entity(imageBean.getImageUrl(imageId)).build();
    }

    @GET
    @Path("/{imageId}")
    public Response getImageMetadata(@PathParam("imageId") Integer imageId) {

        ImageEntity image = imageBean.getImage(imageId);

        if (image == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(image).build();
    }

    @POST
    public Response createImage(ImageEntity image) {
        if (image.getTitle() == null || image.getDescription() == null || image.getHeight() == null || image.getWidth() == null || image.getUri() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            image = imageBean.createImage(image);
        }
        return Response.status(Response.Status.OK).entity(image).build();
    }

    @DELETE
    @Path("/{imageId}")
    public Response deleteImageMetadata(@PathParam("imageId") Integer imageId) {

        boolean deleted = imageBean.deleteImage(imageId);

        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/analyze/{imageId}")
    public Response analyze(@PathParam("imageId") Integer imageId) {
        CompletionStage<String> stringCompletionStage =
                imageAnalysingApi.processImageAsynch(imageId);

        stringCompletionStage.whenCompleteAsync((s, throwable) -> {
            if (throwable != null) {
                log.severe(throwable.getMessage());
            }
            System.out.println(s);
        });

        stringCompletionStage.exceptionally(throwable -> {
            log.severe(throwable.getMessage());
            return throwable.getMessage();
        });

        return Response.status(Response.Status.OK).entity("Slika se analizira!").build();
    }
}

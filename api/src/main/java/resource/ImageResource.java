package resource;

import beans.ImageBean;
import com.kumuluz.ee.logs.cdi.Log;
import entities.ImageEntity;
import interceptors.LogContextInterceptor;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
@Path("/images")
//@Log
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ImageResource {

    @Inject
    private ImageBean imageBean;

    @GET
    @Timed(name = "getMethod_timer")
    public Response getImageList() {
        final List<entities.ImageEntity> imageList = imageBean.getImageList();
        return Response.ok(imageList).build();
    }

    @GET
    @Path("/url/{imageId}")
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
}

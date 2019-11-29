package resource;

import beans.ImageBean;
import entities.ImageEntity;
import entities.MilestoneEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
@Path("/images")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ImageResource {

    @Inject
    private ImageBean imageBean;

    @GET
    public Response getImageList() {
        final List<entities.ImageEntity> imageList = imageBean.getImageList();
        return Response.ok(imageList).build();
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

    @GET
    @Path("/info")
    public Response getInfo() {
        MilestoneEntity mileStone = new MilestoneEntity();
        mileStone.addClan("rk4059");
        mileStone.addClan("Aljoša Omejc");

        mileStone.setOpis_projekta("Najin projekt implementira aplikacijo instagram2");
        mileStone.addMikroStoritev("http://robert.kosir.dev/image-catalog/api/images/");
        mileStone.addMikroStoritev("http://robert.kosir.dev/comments/api/comments/");

        mileStone.addGitHubLink("https://github.com/roalj/image-catalog");
        mileStone.addGitHubLink("https://github.com/roalj/comments");

        mileStone.addTravisLink("https://travis-ci.org/roalj/image-catalog");
        mileStone.addTravisLink("https://travis-ci.org/roalj/comments");

        mileStone.addGitHubLink("https://hub.docker.com/repository/docker/rkosir123/image-catalog");
        mileStone.addGitHubLink("https://hub.docker.com/repository/docker/rkosir123/comments");

        return Response.status(Response.Status.OK).entity(mileStone).build();
    }
}

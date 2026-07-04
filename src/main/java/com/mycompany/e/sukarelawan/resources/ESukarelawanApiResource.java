package com.mycompany.e.sukarelawan.resources;

import com.mycompany.e.sukarelawan.resources.ApiModels.ApplicationRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.ErrorMessage;
import com.mycompany.e.sukarelawan.resources.ApiModels.FeedbackReplyRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.FeedbackRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.HourRecord;
import com.mycompany.e.sukarelawan.resources.ApiModels.LoginRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.Opportunity;
import com.mycompany.e.sukarelawan.resources.ApiModels.ProfileRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.RegisterRequest;
import com.mycompany.e.sukarelawan.resources.ApiModels.ReviewRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ESukarelawanApiResource {
    private static final EVolunteerStore STORE = EVolunteerStoreFactory.create();

    @GET
    @Path("state")
    public Response state() {
        return Response.ok(STORE.state()).build();
    }

    @POST
    @Path("auth/login")
    public Response login(LoginRequest request) {
        return handle(() -> Response.ok(STORE.login(request)).build());
    }

    @POST
    @Path("auth/register")
    public Response register(RegisterRequest request) {
        return handle(() -> Response.ok(STORE.register(request)).build());
    }

    @POST
    @Path("opportunities")
    public Response createOpportunity(Opportunity opportunity) {
        return handle(() -> Response.ok(STORE.createOpportunity(opportunity)).build());
    }

    @PUT
    @Path("opportunities/{id}")
    public Response updateOpportunity(@PathParam("id") int id, Opportunity opportunity) {
        return handle(() -> Response.ok(STORE.updateOpportunity(id, opportunity)).build());
    }

    @DELETE
    @Path("opportunities/{id}")
    public Response deleteOpportunity(@PathParam("id") int id) {
        return handle(() -> {
            STORE.deleteOpportunity(id);
            return Response.noContent().build();
        });
    }

    @POST
    @Path("applications")
    public Response apply(ApplicationRequest request) {
        return handle(() -> {
            STORE.apply(request);
            return Response.noContent().build();
        });
    }

    @POST
    @Path("hours")
    public Response submitHours(HourRecord record) {
        return handle(() -> Response.ok(STORE.submitHours(record)).build());
    }

    @POST
    @Path("hours/{id}/{action}")
    public Response reviewHours(@PathParam("id") int id, @PathParam("action") String action, ReviewRequest request) {
        return handle(() -> Response.ok(STORE.reviewHours(id, action, request.adminId)).build());
    }

    @POST
    @Path("applications/{id}/{action}")
    public Response reviewApplication(@PathParam("id") int id, @PathParam("action") String action, ReviewRequest request) {
        return handle(() -> Response.ok(STORE.reviewApplication(id, action, request.adminId)).build());
    }

    @PUT
    @Path("users/{id}/profile")
    public Response updateProfile(@PathParam("id") int id, ProfileRequest request) {
        return handle(() -> Response.ok(STORE.updateProfile(id, request)).build());
    }

    @POST
    @Path("feedback")
    public Response submitFeedback(FeedbackRequest request) {
        return handle(() -> Response.ok(STORE.submitFeedback(request)).build());
    }

    @POST
    @Path("feedback/{id}/reply")
    public Response replyFeedback(@PathParam("id") int id, FeedbackReplyRequest request) {
        return handle(() -> Response.ok(STORE.replyFeedback(id, request)).build());
    }

    private Response handle(ResponseSupplier supplier) {
        try {
            return supplier.get();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(exception.getMessage()))
                    .build();
        } catch (RuntimeException exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage(exception.getMessage()))
                    .build();
        }
    }

    private interface ResponseSupplier {
        Response get();
    }
}

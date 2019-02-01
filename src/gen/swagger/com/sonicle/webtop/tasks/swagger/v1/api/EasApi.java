package com.sonicle.webtop.tasks.swagger.v1.api;

import com.sonicle.webtop.tasks.swagger.v1.model.SyncFolder;
import com.sonicle.webtop.tasks.swagger.v1.model.SyncTask;
import com.sonicle.webtop.tasks.swagger.v1.model.SyncTaskStat;
import com.sonicle.webtop.tasks.swagger.v1.model.SyncTaskUpdate;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/eas")
@Api(description = "the eas API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2019-02-01T15:12:39.188+01:00")
public abstract class EasApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @POST
    @Path("/folders/{folderId}/messages")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Adds a message", notes = "", response = SyncTaskStat.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "eas-messages",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Success", response = SyncTaskStat.class) })
    public Response addMessage(@PathParam("folderId") @ApiParam("Folder ID") Integer folderId,@Valid SyncTaskUpdate body) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/folders/{folderId}/messages/{id}")
    @ApiOperation(value = "Deletes a message", notes = "", response = Void.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "eas-messages",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Success", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Message not found", response = Void.class) })
    public Response deleteMessage(@PathParam("folderId") @ApiParam("Folder ID") Integer folderId,@PathParam("id") @ApiParam("Message ID") Integer id) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/folders")
    @Produces({ "application/json" })
    @ApiOperation(value = "List all folders", notes = "", response = SyncFolder.class, responseContainer = "List", authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "eas-folders",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = SyncFolder.class, responseContainer = "List") })
    public Response getFolders() {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/folders/{folderId}/messages/{id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a single message", notes = "", response = SyncTask.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "eas-messages",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = SyncTask.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Message not found", response = Void.class) })
    public Response getMessage(@PathParam("folderId") @ApiParam("Folder ID") Integer folderId,@PathParam("id") @ApiParam("Message ID") Integer id) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/folders/{folderId}/messages-stats")
    @Produces({ "application/json" })
    @ApiOperation(value = "List all messages for a specific folder", notes = "", response = SyncTaskStat.class, responseContainer = "List", authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "eas-messages",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = SyncTaskStat.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class) })
    public Response getMessagesStats(@PathParam("folderId") @ApiParam("Folder ID") Integer folderId,@QueryParam("cutoffDate")   @ApiParam("Cut-off date (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;)")  String cutoffDate) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/folders/{folderId}/messages/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Updates a message", notes = "", response = SyncTaskStat.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "eas-messages" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = SyncTaskStat.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Message not found", response = Void.class) })
    public Response updateMessage(@PathParam("folderId") @ApiParam("Folder ID") Integer folderId,@PathParam("id") @ApiParam("Message ID") Integer id,@Valid SyncTaskUpdate body) {
        return Response.ok().entity("magic!").build();
    }
}

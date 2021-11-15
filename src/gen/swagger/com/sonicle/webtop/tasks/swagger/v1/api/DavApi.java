package com.sonicle.webtop.tasks.swagger.v1.api;

import com.sonicle.webtop.tasks.swagger.v1.model.DavFolder;
import com.sonicle.webtop.tasks.swagger.v1.model.DavFolderNew;
import com.sonicle.webtop.tasks.swagger.v1.model.DavFolderUpdate;
import com.sonicle.webtop.tasks.swagger.v1.model.DavObject;
import com.sonicle.webtop.tasks.swagger.v1.model.DavObjectPayload;
import com.sonicle.webtop.tasks.swagger.v1.model.DavObjectsChanges;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/dav")
@Api(description = "the dav API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2021-11-15T13:50:30.198+01:00")
public abstract class DavApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @POST
    @Path("/folders")
    @ApiOperation(value = "Adds new task-folder", notes = "Adds a new task category.", response = DavFolder.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Success", response = DavFolder.class) })
    public Response addDavFolder(@Valid DavFolderNew body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/folders/{folderUid}/objects")
    @ApiOperation(value = "Add new task object", notes = "Adds new task object.", response = Void.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Success", response = Void.class) })
    public Response addDavObject(@PathParam("folderUid") String folderUid,@Valid DavObjectPayload body) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/folders/{folderUid}")
    @ApiOperation(value = "Delete task-folder", notes = "Deletes the specified task category.", response = Void.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Folder deleted", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Folder not Found", response = Void.class),
        @ApiResponse(code = 405, message = "Delete operation not allowed", response = Void.class) })
    public Response deleteDavFolder(@PathParam("folderUid") String folderUid) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/folders/{folderUid}/objects/{href}")
    @ApiOperation(value = "Deletes task object", notes = "Deletes the task object at the specified href.", response = Void.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Object not found", response = Void.class) })
    public Response deleteDavObject(@PathParam("folderUid") String folderUid,@PathParam("href") String href) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/folders/{folderUid}")
    @ApiOperation(value = "Get task-folder", notes = "Gets the specified task category.", response = DavFolder.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = DavFolder.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Folder not found", response = Void.class) })
    public Response getDavFolder(@PathParam("folderUid") String folderUid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/folders")
    @ApiOperation(value = "List task-folders", notes = "Returns a list of available task categories.", response = DavFolder.class, responseContainer = "List", authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = DavFolder.class, responseContainer = "List") })
    public Response getDavFolders() {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/folders/{folderUid}/objects/{href}")
    @ApiOperation(value = "Get task object", notes = "Gets the task object at the specified href.", response = DavObject.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = DavObject.class),
        @ApiResponse(code = 400, message = "Invalid parameters", response = Void.class),
        @ApiResponse(code = 404, message = "Object not found", response = Void.class) })
    public Response getDavObject(@PathParam("folderUid") String folderUid,@PathParam("href") String href,@QueryParam("format")   @ApiParam("Desired format of object data payload (defaults to icalendar)")  String format) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/folders/{folderUid}/objects")
    @ApiOperation(value = "List task objects", notes = "Returns a list of task objects for the specified folder.", response = DavObject.class, responseContainer = "List", authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = DavObject.class, responseContainer = "List") })
    public Response getDavObjects(@PathParam("folderUid") String folderUid,@QueryParam("hrefs")   @ApiParam("A collection of hrefs to get")  List<String> hrefs,@QueryParam("format")   @ApiParam("Desired format of object data payload (defaults to icalendar)")  String format) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/folders/{folderUid}/objects/changes")
    @ApiOperation(value = "Gets task object changes", notes = "Returns changed task objects (added/modified/deleted) since the specified sync-token. If sync-token is not provided, the full set (initial) will be returned.", response = DavObjectsChanges.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = DavObjectsChanges.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class) })
    public Response getDavObjectsChanges(@PathParam("folderUid") String folderUid,@QueryParam("syncToken")   @ApiParam("Defines changes starting point")  String syncToken,@QueryParam("limit")   @ApiParam("Limits the number of returned results")  Integer limit) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/folders/{folderUid}")
    @ApiOperation(value = "Update task-folder", notes = "Updates the specified task category.", response = Void.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Folder updated", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Folder not Found", response = Void.class) })
    public Response updateDavFolder(@PathParam("folderUid") String folderUid,@Valid DavFolderUpdate body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/folders/{folderUid}/objects/{href}")
    @ApiOperation(value = "Update task object", notes = "Updates the task object at the specified href.", response = Void.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class) })
    public Response updateDavObject(@PathParam("folderUid") String folderUid,@PathParam("href") String href,@Valid DavObjectPayload body) {
        return Response.ok().entity("magic!").build();
    }
}

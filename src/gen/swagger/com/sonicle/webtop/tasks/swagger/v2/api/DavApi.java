package com.sonicle.webtop.tasks.swagger.v2.api;

import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavFolder;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavFolderNew;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavFolderUpdate;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavObject;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavObjectPayload;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavObjectsChanges;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/dav/folders")
@Api(description = "the dav API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-14T18:05:51.753+02:00[Europe/Berlin]")
public abstract class DavApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @POST
    @Produces({ "*/*" })
    @ApiOperation(value = "Adds new task-folder", notes = "Creates new Category.", response = ApiDavFolder.class, authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Success", response = ApiDavFolder.class)
    })
    public Response addDavFolder(@Valid ApiDavFolderNew body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/{folderUid}/objects")
    @ApiOperation(value = "Add new task object", notes = "Creates new Task into specified Category.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Success", response = Void.class)
    })
    public Response addDavObject(@PathParam("folderUid") String folderUid,@Valid ApiDavObjectPayload body) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/{folderUid}")
    @Produces({ "*/*" })
    @ApiOperation(value = "Delete task-folder", notes = "Deletes the specified Category.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Folder deleted", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Object.class),
        @ApiResponse(code = 404, message = "Folder not Found", response = Object.class),
        @ApiResponse(code = 405, message = "Delete operation not allowed", response = Object.class)
    })
    public Response deleteDavFolder(@PathParam("folderUid") String folderUid) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/{folderUid}/objects/{href}")
    @Produces({ "*/*" })
    @ApiOperation(value = "Deletes task object", notes = "Deletes the specified Task.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Object.class),
        @ApiResponse(code = 404, message = "Object not found", response = Object.class)
    })
    public Response deleteDavObject(@PathParam("folderUid") String folderUid,@PathParam("href") String href) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/{folderUid}")
    @Produces({ "*/*" })
    @ApiOperation(value = "Get task folder", notes = "Gets the specified Category.", response = ApiDavFolder.class, authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiDavFolder.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Object.class),
        @ApiResponse(code = 404, message = "Folder not found", response = Object.class)
    })
    public Response getDavFolder(@PathParam("folderUid") String folderUid) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Produces({ "*/*" })
    @ApiOperation(value = "List task folders", notes = "Returns available Categories.", response = ApiDavFolder.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiDavFolder.class, responseContainer = "List")
    })
    public Response getDavFolders() {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/{folderUid}/objects/{href}")
    @Produces({ "*/*" })
    @ApiOperation(value = "Get task object", notes = "Gets the specified Task.", response = ApiDavObject.class, authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiDavObject.class),
        @ApiResponse(code = 400, message = "Invalid parameters", response = Object.class),
        @ApiResponse(code = 404, message = "Object not found", response = Object.class)
    })
    public Response getDavObject(@PathParam("folderUid") String folderUid,@PathParam("href") String href,@QueryParam("format")  @ApiParam("Desired format of object data payload (defaults to icalendar)")  String format) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/{folderUid}/objects")
    @Produces({ "*/*" })
    @ApiOperation(value = "List task objects", notes = "List all Tasks of specified Category.", response = ApiDavObject.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiDavObject.class, responseContainer = "List")
    })
    public Response getDavObjects(@PathParam("folderUid") String folderUid,@QueryParam("hrefs")  @ApiParam("A collection of hrefs to get")  List<String> hrefs,@QueryParam("format")  @ApiParam("Desired format of object data payload (defaults to icalendar)")  String format) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/{folderUid}/objects/changes")
    @Produces({ "*/*" })
    @ApiOperation(value = "Gets task object changes", notes = "Returns changed task objects (added/modified/deleted) since the specified sync-token. If sync-token is not provided, the full set (initial) will be returned.", response = ApiDavObjectsChanges.class, authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiDavObjectsChanges.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Object.class)
    })
    public Response getDavObjectsChanges(@PathParam("folderUid") String folderUid,@QueryParam("syncToken")  @ApiParam("Defines changes starting point")  String syncToken,@QueryParam("limit")  @ApiParam("Limits the number of returned results")  Integer limit) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/{folderUid}")
    @Produces({ "*/*" })
    @ApiOperation(value = "Update task-folder", notes = "Updates the specified Category.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Folder updated", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Object.class),
        @ApiResponse(code = 404, message = "Folder not Found", response = Object.class)
    })
    public Response updateDavFolder(@PathParam("folderUid") String folderUid,@Valid ApiDavFolderUpdate body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/{folderUid}/objects/{href}")
    @ApiOperation(value = "Update task object", notes = "Updates the specified Task.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-basic"),
        
        @Authorization(value = "auth-bearer")
         }, tags={ "dav" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class)
    })
    public Response updateDavObject(@PathParam("folderUid") String folderUid,@PathParam("href") String href,@Valid ApiDavObjectPayload body) {
        return Response.ok().entity("magic!").build();
    }
}

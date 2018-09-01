package org.dobrien.watson.services.endpoints.impl;

import org.dobrien.watson.services.endpoints.*;
import org.dobrien.watson.services.model.*;

import org.dobrien.watson.services.model.Error;
import org.dobrien.watson.services.model.User;

import java.util.List;
import org.dobrien.watson.services.endpoints.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-08-13T16:54:53.643+12:00")
public class UserApiServiceImpl extends UserApiService {
    @Override
    public Response getUser(SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response loginUser( @NotNull String name,  String tag, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}

package org.dobrien.watson.services.endpoints.factories;

import org.dobrien.watson.services.endpoints.UserApiService;
import org.dobrien.watson.services.endpoints.impl.UserApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-09-01T15:02:02.417+12:00")
public class UserApiServiceFactory {
    private final static UserApiService service = new UserApiServiceImpl();

    public static UserApiService getUserApi() {
        return service;
    }
}

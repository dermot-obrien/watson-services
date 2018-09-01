package org.dobrien.watson.services.endpoints.factories;

import org.dobrien.watson.services.endpoints.UserApiService;
import org.dobrien.watson.services.endpoints.impl.UserApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-08-13T16:54:53.643+12:00")
public class UserApiServiceFactory {
    private final static UserApiService service = new UserApiServiceImpl();

    public static UserApiService getUserApi() {
        return service;
    }
}

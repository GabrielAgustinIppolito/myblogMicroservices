package it.cgmconsulting.gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteValidator {

//    public static final String openApiEndpoint = "v0"; //indica la chiamata che non ha bisogno autenticazione

    public boolean isOpenEndpoint(ServerHttpRequest request){
        return request.getURI().getPath().contains("v0") || request.getURI().getPath().contains("actuator");
    }

}

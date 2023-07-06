package it.cgmconsulting.gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/ms-auth/signup",
            "/ms-auth/signin"
    );

    public boolean isOpenEndpoint(ServerHttpRequest request){
        return openApiEndpoints.contains(request.getURI().getPath());
    }

}

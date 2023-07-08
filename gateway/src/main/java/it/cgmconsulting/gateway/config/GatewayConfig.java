package it.cgmconsulting.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final AuthenticationFilter filter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("ms-auth", r -> r.path("/ms-auth/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://ms-auth"))
                .route("ms-post", r -> r.path("/ms-post/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://ms-post"))
                .build();
    }

}
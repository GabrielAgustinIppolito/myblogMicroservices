package it.cgmconsulting.gateway;

import it.cgmconsulting.gateway.config.AuthenticationFilter;
import it.cgmconsulting.gateway.config.JwtTokenProvider;
import it.cgmconsulting.gateway.config.RouteValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}

package com.example.demo.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.filters.MyGatewayFilter;

@Configuration
public class RouteConfig {

	@Bean
	RouteLocator handleRoutes(RouteLocatorBuilder builder, MyGatewayFilter custom) {
		return builder.routes()
				// Product Catalog Service - JWT required
				.route(p -> p.path("/api/v1/products/**")
						.filters(f -> f.filter(custom))
						.uri("lb://product-catalog-service"))
				// Order Management Service - JWT required
				.route(p -> p.path("/api/v1/orders/**")
						.filters(f -> f.filter(custom))
						.uri("lb://order-management-service"))
				// Auth endpoints - no JWT (legacy/external services)
				.route(p -> p.path("/api/v1/auth/**").uri("lb://JWT-AUTH-SERVICE"))
				.build();
	}
}



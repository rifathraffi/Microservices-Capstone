package com.example.demo.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.demo.config.RouterValidator;
import com.example.demo.utils.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import reactor.core.publisher.Mono;


@Component

public class MyGatewayFilter implements GatewayFilter {

		private JwtUtil jwtUtil;

		private RouterValidator validator;
		
		

		


		public MyGatewayFilter(JwtUtil jwtUtil, RouterValidator validator) {
			super();
			this.jwtUtil = jwtUtil;
			this.validator = validator;
		}

		@Override
		public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
			
			
	        ServerHttpRequest request = exchange.getRequest();
	        
	        if (validator.isSecured.test(request)) {

	            if (!request.getHeaders().containsHeader("Authorization")) {
	                return this.onError(exchange, "Authorization header is missing", HttpStatus.UNAUTHORIZED);
	            }

	            String authHeader = request.getHeaders().getFirst("Authorization");
	            
	            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	                return this.onError(exchange, "Invalid Authorization format", HttpStatus.UNAUTHORIZED);
	            }

	            final String token = authHeader.substring(7);


	            if (!jwtUtil.isTokenValid(token)) {
	                return this.onError(exchange, "JWT validation failed or token is expired", HttpStatus.UNAUTHORIZED);
	            }

	            String username = jwtUtil.extractUsername(token);
	            String role = jwtUtil.extractRole(token);

	            // USER role cannot list orders by customer (GET /api/v1/orders?customerId=...)
	            if ("USER".equals(role) && isListOrdersByCustomer(request)) {
	                return this.onError(exchange, "Forbidden: USER role cannot list orders by customer", HttpStatus.FORBIDDEN);
	            }

	            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
	                    .header("X-Auth-User", username)
	                    .header("X-Auth-Role", role)
	                    .build();

	            return chain.filter(exchange.mutate().request(mutatedRequest).build());
	        }

	        return chain.filter(exchange);
	    }
	    
	    private boolean isListOrdersByCustomer(ServerHttpRequest request) {
	        return "GET".equalsIgnoreCase(request.getMethod().name())
	                && request.getURI().getPath().contains("/api/v1/orders")
	                && request.getQueryParams().containsKey("customerId");
	    }

	    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
	        ServerHttpResponse response = exchange.getResponse();
	        response.setStatusCode(httpStatus);
	        return response.setComplete();
	    }	
	    
}




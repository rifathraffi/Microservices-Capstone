package com.example.demo.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.*;

@Component
public class RouterValidator {
	    public static final List<String> whiteLabels = List.of(
			"/api/v1/auth/register", "/api/v1/auth/login",
			"/actuator", "/actuator/health");

	    public Predicate<ServerHttpRequest> isSecured = request -> whiteLabels.stream()
	                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
	}


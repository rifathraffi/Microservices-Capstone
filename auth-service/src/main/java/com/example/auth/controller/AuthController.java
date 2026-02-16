package com.example.auth.controller;

import com.example.auth.model.AuthResponse;
import com.example.auth.model.LoginRequest;
import com.example.auth.model.RegisterRequest;
import com.example.auth.service.JwtTokenService;
import com.example.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final UserService userService;
	private final JwtTokenService jwtTokenService;

	public AuthController(UserService userService, JwtTokenService jwtTokenService) {
		this.userService = userService;
		this.jwtTokenService = jwtTokenService;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
		return userService.authenticate(request.username(), request.password())
				.<ResponseEntity<?>>map(auth -> {
					String token = jwtTokenService.generateToken(auth.username(), auth.role().name());
					return ResponseEntity.ok(AuthResponse.of(token, auth.username(), auth.role().name()));
				})
				.orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("error", "Invalid username or password")));
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
		if (!userService.register(request)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("error", "Username already exists"));
		}
		// New registrations get USER role
		String token = jwtTokenService.generateToken(request.username(), "USER");
		return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.of(token, request.username(), "USER"));
	}
}

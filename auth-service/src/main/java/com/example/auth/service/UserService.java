package com.example.auth.service;

import com.example.auth.model.RegisterRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

	// In-memory user store: username -> password (for demo only; use BCrypt in production)
	private final Map<String, String> users = new ConcurrentHashMap<>();

	public UserService() {
		// Demo user for testing
		users.put("admin", "admin123");
		users.put("user", "user123");
	}

	public Optional<String> authenticate(String username, String password) {
		String stored = users.get(username);
		if (stored != null && stored.equals(password)) {
			return Optional.of(username);
		}
		return Optional.empty();
	}

	public boolean register(RegisterRequest request) {
		if (users.containsKey(request.username())) {
			return false;
		}
		users.put(request.username(), request.password());
		return true;
	}
}

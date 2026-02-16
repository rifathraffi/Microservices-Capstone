package com.example.auth.service;

import com.example.auth.model.RegisterRequest;
import com.example.auth.model.Role;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

	// In-memory: username -> UserInfo (for demo only; use BCrypt in production)
	private final Map<String, UserInfo> users = new ConcurrentHashMap<>();

	public UserService() {
		users.put("admin", new UserInfo("admin123", Role.ADMIN));
		users.put("user", new UserInfo("user123", Role.USER));
	}

	public Optional<AuthResult> authenticate(String username, String password) {
		UserInfo info = users.get(username);
		if (info != null && info.password.equals(password)) {
			return Optional.of(new AuthResult(username, info.role));
		}
		return Optional.empty();
	}

	public boolean register(RegisterRequest request) {
		if (users.containsKey(request.username())) {
			return false;
		}
		users.put(request.username(), new UserInfo(request.password(), Role.USER));
		return true;
	}

	public Optional<Role> getRole(String username) {
		UserInfo info = users.get(username);
		return info != null ? Optional.of(info.role) : Optional.empty();
	}

	public record AuthResult(String username, Role role) {}
	private record UserInfo(String password, Role role) {}
}

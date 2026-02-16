package com.example.auth.model;

public record AuthResponse(
	String token,
	String username,
	String role,
	String type
) {
	public static AuthResponse of(String token, String username, String role) {
		return new AuthResponse(token, username, role, "Bearer");
	}
}

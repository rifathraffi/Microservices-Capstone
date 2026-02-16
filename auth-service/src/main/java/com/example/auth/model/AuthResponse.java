package com.example.auth.model;

public record AuthResponse(
	String token,
	String username,
	String type
) {
	public static AuthResponse of(String token, String username) {
		return new AuthResponse(token, username, "Bearer");
	}
}

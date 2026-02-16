package com.example.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtTokenService {

	@Value("${jwt.secret}")
	private String secret;

	private SecretKey key;

	@PostConstruct
	public void init() {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateToken(String username, String role) {
		long now = System.currentTimeMillis();
		long expiry = now + 24 * 60 * 60 * 1000; // 24 hours

		return Jwts.builder()
				.setSubject(username)
				.claim("role", role != null ? role : "USER")
				.setIssuedAt(new Date(now))
				.setExpiration(new Date(expiry))
				.signWith(key)
				.compact();
	}
}

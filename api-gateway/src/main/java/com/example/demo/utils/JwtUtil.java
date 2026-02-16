package com.example.demo.utils;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret; 

    private SecretKey key;

    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    private Claims getAllClaimsFromToken(String token) {
    	
    	return Jwts.parserBuilder()
    	           .setSigningKey(key)
    	           .build()
    	           .parseClaimsJws(token)
    	           .getBody();
    	 
    }

    public boolean isTokenExpired(String token) {
    	
    	
        return this.getAllClaimsFromToken(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token) {
        try {
            this.getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public String extractRole(String token) {
        Object role = getAllClaimsFromToken(token).get("role");
        return role != null ? role.toString() : "USER";
    }
}
package co.com.crediya.api.dto;

public record TokenResponse(String tokenType, String accessToken, long expiresInSeconds) {}

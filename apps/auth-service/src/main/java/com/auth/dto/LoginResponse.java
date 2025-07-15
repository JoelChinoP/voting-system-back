package com.auth.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private long expiresIn;
    private String message = "Authentication successful";

    public LoginResponse(String token) {
        this.token = token;
        this.expiresIn = 86400; // 24 horas en segundos
    }
    
    public LoginResponse(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }
}

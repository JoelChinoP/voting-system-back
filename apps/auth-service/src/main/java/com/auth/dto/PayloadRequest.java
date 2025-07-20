package com.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayloadRequest {
    @NotBlank(message = "Token is required")
    private String token;
}
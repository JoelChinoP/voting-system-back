package com.auth.dto;

import jakarta.validation.constraints.*;

public class RegisterRequest {

    @NotBlank @Size(min = 2, max = 100)
    private String fullName;
    
    @NotBlank @Email
    private String username;        // Usamos el email como username
    
    @NotBlank 
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
      regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*$",
      message = "La contraseña debe incluir mayúsculas, minúsculas y números"
    )
    private String password;

    // getters + setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

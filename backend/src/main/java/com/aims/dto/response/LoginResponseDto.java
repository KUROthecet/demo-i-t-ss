package com.aims.dto.response;

public class LoginResponseDto {

    private String token;
    private Long   userId;
    private String username;
    private String role;
    private String fullName;

    public LoginResponseDto() {}

    public LoginResponseDto(String token, Long userId, String username, String role, String fullName) {
        this.token    = token;
        this.userId   = userId;
        this.username = username;
        this.role     = role;
        this.fullName = fullName;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}

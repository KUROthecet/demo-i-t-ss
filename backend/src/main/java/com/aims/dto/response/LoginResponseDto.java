package com.aims.dto.response;

import java.util.Set;

public class LoginResponseDto {

    private String token;
    private Long   userId;
    private String username;
    private Set<String> roles;
    private String fullName;
    private String avatarUrl;

    public LoginResponseDto() {}

    public LoginResponseDto(String token, Long userId, String username, Set<String> roles, String fullName, String avatarUrl) {
        this.token    = token;
        this.userId   = userId;
        this.username = username;
        this.roles    = roles;
        this.fullName = fullName;
        this.avatarUrl= avatarUrl;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}

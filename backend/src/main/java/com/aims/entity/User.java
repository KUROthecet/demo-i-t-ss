package com.aims.entity;

import com.aims.exception.BusinessException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    public static final String ROLE_ADMIN           = "ADMIN";
    public static final String ROLE_PRODUCT_MANAGER = "PRODUCT_MANAGER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(unique = true)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    private String status = "ACTIVE";
    private String fullName;
    private String phone;
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String blockReason;

    public void block(String reason) {
        this.status      = "BLOCKED";
        this.blockReason = reason;
    }

    public void unblock() {
        this.status      = "ACTIVE";
        this.blockReason = null;
    }

    public void deactivate() {
        this.status = "DEACTIVATED";
    }

    public boolean canLogin() {
        return "ACTIVE".equals(this.status);
    }

    public boolean isAdmin() {
        return this.roles.contains(ROLE_ADMIN);
    }

    public boolean isManager() {
        return this.roles.contains(ROLE_PRODUCT_MANAGER);
    }

    public void setRoles(Set<String> newRoles) {
        if (newRoles == null || newRoles.isEmpty()) {
            throw new BusinessException("A user must have at least one role.");
        }
        for (String r : newRoles) {
            if (!ROLE_ADMIN.equals(r) && !ROLE_PRODUCT_MANAGER.equals(r)) {
                throw new BusinessException("Invalid role: " + r + ". Must be ADMIN or PRODUCT_MANAGER.");
            }
        }
        this.roles = new HashSet<>(newRoles);
    }
}

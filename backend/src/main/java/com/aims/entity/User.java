package com.aims.entity;

import com.aims.exception.BusinessException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

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

    @NotBlank
    private String role;

    private String status = "ACTIVE";
    private String fullName;
    private String phone;

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
        return "ADMIN".equals(this.role);
    }

    public boolean isManager() {
        return "PRODUCT_MANAGER".equals(this.role);
    }

    public void changeRole(String newRole) {
        if (!"ADMIN".equals(newRole) && !"PRODUCT_MANAGER".equals(newRole)) {
            throw new BusinessException("Invalid role: " + newRole + ". Must be ADMIN or PRODUCT_MANAGER.");
        }
        this.role = newRole;
    }
}

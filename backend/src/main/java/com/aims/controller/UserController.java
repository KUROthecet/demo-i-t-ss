package com.aims.controller;

import com.aims.dto.request.UserCreateDto;
import com.aims.dto.response.UserResponseDto;
import com.aims.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(
            userService.getAllUsers().stream()
                .map(UserResponseDto::fromEntity)
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponseDto.fromEntity(userService.getUserById(id)));
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto dto) {
        return ResponseEntity.ok(UserResponseDto.fromEntity(userService.createUser(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserCreateDto dto) {
        return ResponseEntity.ok(UserResponseDto.fromEntity(userService.updateUser(id, dto)));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<UserResponseDto> blockUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Blocked by admin");
        return ResponseEntity.ok(UserResponseDto.fromEntity(userService.blockUser(id, reason)));
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<UserResponseDto> unblockUser(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponseDto.fromEntity(userService.unblockUser(id)));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<UserResponseDto> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponseDto.fromEntity(userService.deactivateUser(id)));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return ResponseEntity.ok(Map.of("message", "Password reset. New password sent via email."));
    }

    @PostMapping("/{id}/change-role")
    public ResponseEntity<UserResponseDto> changeRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String role = body.getOrDefault("role", "PRODUCT_MANAGER");
        return ResponseEntity.ok(UserResponseDto.fromEntity(userService.changeRole(id, role)));
    }
}

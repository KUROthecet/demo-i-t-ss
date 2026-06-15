package com.aims.controller;

import com.aims.dto.request.LoginRequestDto;
import com.aims.dto.response.LoginResponseDto;
import com.aims.entity.User;
import com.aims.security.JwtTokenProvider;
import com.aims.security.TokenBlacklist;
import com.aims.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserDetailsService userDetailsService;
    private final UserService        userService;
    private final PasswordEncoder    passwordEncoder;
    private final JwtTokenProvider   jwtTokenProvider;
    private final TokenBlacklist     tokenBlacklist;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getUsername());

        if (!passwordEncoder.matches(dto.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user    = userService.getUserByUsername(dto.getUsername());
        String token = jwtTokenProvider.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponseDto(
                token, user.getId(), user.getUsername(), user.getRole(), user.getFullName(), user.getAvatarUrl()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                tokenBlacklist.add(token, jwtTokenProvider.getRemainingExpiryMs(token));
            }
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}

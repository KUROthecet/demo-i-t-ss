package com.aims.controller;

import com.aims.dto.request.LoginRequestDto;
import com.aims.dto.response.LoginResponseDto;
import com.aims.entity.User;
import com.aims.repository.UserRepository;
import com.aims.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserDetailsService userDetailsService;
    private final UserRepository     userRepository;
    private final PasswordEncoder    passwordEncoder;
    private final JwtTokenProvider   jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getUsername());

        if (!passwordEncoder.matches(dto.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        String token = jwtTokenProvider.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponseDto(
            token, user.getId(), user.getUsername(), user.getRole(), user.getFullName()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // JWT is stateless — client simply discards the token
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}

package com.aims.service;

import com.aims.dto.request.UserCreateDto;
import com.aims.entity.User;
import com.aims.exception.BusinessException;
import com.aims.exception.ResourceNotFoundException;
import com.aims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService    emailService;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        Optional<User> found = userRepository.findById(id);
        if (found.isEmpty()) {
            throw new ResourceNotFoundException("User", id);
        }
        return found.get();
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        Optional<User> found = userRepository.findByUsername(username);
        if (found.isEmpty()) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        return found.get();
    }

    public User createUser(UserCreateDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("Username '" + dto.getUsername() + "' is already taken.");
        }
        if (dto.getEmail() != null && userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email '" + dto.getEmail() + "' is already registered.");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    public User updateUser(Long id, UserCreateDto dto) {
        User user = getUserById(id);
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getPhone()    != null) user.setPhone(dto.getPhone());
        if (dto.getEmail()    != null) user.setEmail(dto.getEmail());
        if (dto.getAvatarUrl()!= null) user.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        return userRepository.save(user);
    }

    public User blockUser(Long id, String reason) {
        User user = getUserById(id);
        user.block(reason);
        return userRepository.save(user);
    }

    public User unblockUser(Long id) {
        User user = getUserById(id);
        user.unblock();
        return userRepository.save(user);
    }

    public User deactivateUser(Long id) {
        User user = getUserById(id);
        user.deactivate();
        return userRepository.save(user);
    }

    public User resetPassword(Long id) {
        User user = getUserById(id);
        String tempPassword = "AIMS@" + String.format("%06d", new Random().nextInt(999_999));
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        User saved = userRepository.save(user);
        emailService.sendPasswordReset(user.getEmail(), user.getFullName(), tempPassword);
        return saved;
    }

    public User changeRole(Long id, String newRole) {
        User user = getUserById(id);
        user.changeRole(newRole);
        return userRepository.save(user);
    }
}

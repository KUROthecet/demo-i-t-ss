/*
Coupling level: Content Coupling
Reason why: UserServiceImpl fully constructs the entity piece-by-piece via public setters
*/

package com.aims.service.impl;

import com.aims.dto.request.UserCreateDto;
import com.aims.entity.User;
import com.aims.exception.BusinessException;
import com.aims.exception.ResourceNotFoundException;
import com.aims.repository.UserRepository;
import com.aims.service.EmailService;
import com.aims.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService    emailService;

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
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
        user.setStatus("ACTIVE");

        User saved = userRepository.save(user);
        log.info("User created: {} ({})", saved.getUsername(), saved.getRole());
        return saved;
    }

    @Override
    public User updateUser(Long id, UserCreateDto dto) {
        User user = getUserById(id);
        if (dto.getFullName()  != null) user.setFullName(dto.getFullName());
        if (dto.getPhone()     != null) user.setPhone(dto.getPhone());
        if (dto.getEmail()     != null) user.setEmail(dto.getEmail());
        if (dto.getPassword()  != null && !dto.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public User blockUser(Long id, String reason) {
        User user = getUserById(id);
        user.block(reason);
        User saved = userRepository.save(user);
        log.info("User blocked: {} | Reason: {}", user.getUsername(), reason);
        return saved;
    }

    @Override
    public User unblockUser(Long id) {
        User user = getUserById(id);
        user.unblock();
        User saved = userRepository.save(user);
        log.info("User unblocked: {}", user.getUsername());
        return saved;
    }

    @Override
    public User deactivateUser(Long id) {
        User user = getUserById(id);
        user.deactivate();
        User saved = userRepository.save(user);
        log.info("User deactivated: {}", user.getUsername());
        return saved;
    }

    @Override
    public User resetPassword(Long id) {
        User user = getUserById(id);
        // "AIMS@" + 6 random digits — temporary password format
        String tempPassword = "AIMS@" + String.format("%06d", new Random().nextInt(999_999));
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        User saved = userRepository.save(user);

        emailService.sendPasswordReset(user.getEmail(), user.getFullName(), tempPassword);
        log.info("Password reset for: {} | Temp password logged in email service", user.getUsername());
        return saved;
    }

    @Override
    public User changeRole(Long id, String newRole) {
        User user = getUserById(id);
        user.changeRole(newRole);
        User saved = userRepository.save(user);
        log.info("Role changed for {}: {}", user.getUsername(), newRole);
        return saved;
    }
}

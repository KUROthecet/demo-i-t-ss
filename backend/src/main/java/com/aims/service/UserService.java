package com.aims.service;

import com.aims.dto.request.UserCreateDto;
import com.aims.entity.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User createUser(UserCreateDto dto);
    User updateUser(Long id, UserCreateDto dto);
    User blockUser(Long id, String reason);
    User unblockUser(Long id);
    User deactivateUser(Long id);
    User resetPassword(Long id);
    User changeRole(Long id, String newRole);
}

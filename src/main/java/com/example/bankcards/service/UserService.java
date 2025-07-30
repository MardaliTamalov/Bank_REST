package com.example.bankcards.service;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.User;

public interface UserService {

    UserResponseDto createUser(UserRequestDto user);

    User createUser(String username, String password, String roleId);

    UserResponseDto getUserById(Long id);

    UserResponseDto updateUser(Long id, UserRequestDto user);

    void deleteById(Long id);

    User findByUsername(String username);

    boolean existsByUsername(String username);
}

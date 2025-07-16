package com.example.bankcards.service;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;

public interface UserService {


    UserResponseDto createUser(UserRequestDto user);

    UserResponseDto getUserById(Long id);

    UserResponseDto updateUser(Long id, UserRequestDto user);

    void deleteById(Long id);
}

package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;

public record UserRequestDto(String username,
                             String password,
                             String roleId){
}


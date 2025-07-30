package com.example.bankcards.mapper;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring")

public interface UserMapper {

    @Mapping(target = "role", ignore = true)
   User requestToUser(UserRequestDto userRequestDto);

    @Mapping(target = "roleId", source = "role.id")
    UserResponseDto userToUserResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateUserFromDto(UserRequestDto dto, @MappingTarget User user);

 }
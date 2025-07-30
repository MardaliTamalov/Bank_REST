package com.example.bankcards.mapper;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-29T22:50:10+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User requestToUser(UserRequestDto userRequestDto) {
        if ( userRequestDto == null ) {
            return null;
        }

        User user = new User();

        user.setUsername( userRequestDto.username() );
        user.setPassword( userRequestDto.password() );

        return user;
    }

    @Override
    public UserResponseDto userToUserResponseDto(User user) {
        if ( user == null ) {
            return null;
        }

        String roleId = null;
        String username = null;
        String password = null;

        roleId = userRoleId( user );
        username = user.getUsername();
        password = user.getPassword();

        UserResponseDto userResponseDto = new UserResponseDto( username, password, roleId );

        return userResponseDto;
    }

    @Override
    public void updateUserFromDto(UserRequestDto dto, User user) {
        if ( dto == null ) {
            return;
        }

        user.setUsername( dto.username() );
        user.setPassword( dto.password() );
    }

    private String userRoleId(User user) {
        if ( user == null ) {
            return null;
        }
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        String id = role.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}

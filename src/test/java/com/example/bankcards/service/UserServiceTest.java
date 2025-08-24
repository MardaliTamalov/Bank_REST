package com.example.bankcards.service;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserShouldSuccessfullyCreateUser() {

        UserRequestDto requestDto = new UserRequestDto("testuser", "password", "1");
        Role role = new Role();
        role.setId("1");
        role.setDescription("USER");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(role);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(role);

        UserResponseDto responseDto = new UserResponseDto("testuser", "encodedPassword", "1");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(roleRepository.findById("1")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userMapper.requestToUser(requestDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.userToUserResponseDto(savedUser)).thenReturn(responseDto);

        UserResponseDto result = userService.createUser(requestDto);

        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals("encodedPassword", result.password());
        assertEquals("1", result.roleId());

        verify(userRepository).findByUsername("testuser");
        verify(roleRepository).findById("1");
        verify(passwordEncoder).encode("password");
        verify(userMapper).requestToUser(requestDto);
        verify(userRepository).save(user);
        verify(userMapper).userToUserResponseDto(savedUser);
    }

    @Test
    void getUserByIdShouldReturnUserWhenExists() {

        Long userId = 1L;
        Role role = new Role();
        role.setId("1");
        role.setDescription("Regular user");

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(role);

        UserResponseDto responseDto = new UserResponseDto("testuser", "password", "1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.userToUserResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals("password", result.password());
        assertEquals("1", result.roleId());

        verify(userRepository).findById(userId);
        verify(userMapper).userToUserResponseDto(user);
    }

    @Test
    void updateUserShouldSuccessfullyUpdateUser() {

        Long userId = 1L;
        UserRequestDto requestDto = new UserRequestDto("updateduser", "newpassword", "2");

        Role oldRole = new Role();
        oldRole.setId("1");
        oldRole.setDescription("Regular user");

        Role newRole = new Role();
        newRole.setId("2");
        newRole.setDescription("Administrator");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("olduser");
        existingUser.setPassword("oldpassword");
        existingUser.setRole(oldRole);

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername("updateduser");
        updatedUser.setPassword("encodedNewPassword");
        updatedUser.setRole(newRole);

        UserResponseDto responseDto = new UserResponseDto("updateduser", "encodedNewPassword", "2");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(roleRepository.findById("2")).thenReturn(Optional.of(newRole));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.userToUserResponseDto(updatedUser)).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(userId, requestDto);

        assertNotNull(result);
        assertEquals("updateduser", result.username());
        assertEquals("encodedNewPassword", result.password());
        assertEquals("2", result.roleId());

        verify(userRepository).findById(userId);
        verify(passwordEncoder).encode("newpassword");
        verify(roleRepository).findById("2");
        verify(userRepository).save(existingUser);
        verify(userMapper).userToUserResponseDto(updatedUser);
    }

    @Test
    void deleteByIdShouldDeleteUserWhenExists() {

        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteById(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void findByUsernameShouldReturnUserWhenExists() {

        String username = "testuser";
        Role role = new Role();
        role.setId("1");
        role.setDescription("Regular user");

        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword("password");
        user.setRole(role);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = userService.findByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("password", result.getPassword());
        assertEquals("1", result.getRole().getId());

        verify(userRepository).findByUsername(username);
    }

    @Test
    void existsByUsernameShouldReturnTrueWhenUserExists() {

        String username = "existinguser";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        boolean result = userService.existsByUsername(username);

        assertTrue(result);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void createUserWithParamsShouldSuccessfullyCreateUser() {

        String username = "testuser";
        String password = "password";
        String roleId = "1";

        Role role = new Role();
        role.setId(roleId);
        role.setDescription("Regular user");

        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");
        user.setRole(role);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(username);
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(role);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(username, password, roleId);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(roleId, result.getRole().getId());

        verify(userRepository).findByUsername(username);
        verify(roleRepository).findById(roleId);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }
}
package com.example.bankcards.service.impl;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        log.info("Creating new user with username: {}", userRequestDto.username());

        if (existsByUsername(userRequestDto.username())) {
            throw new RuntimeException("User with username " + userRequestDto.username() + " already exists");
        }

        log.info("Looking for role with id: {}", userRequestDto.roleId());
        Role role = roleRepository.findById(userRequestDto.roleId())
                .orElseThrow(() -> new RuntimeException("Role with id " + userRequestDto.roleId() + " not found"));
        log.info("Found role: {} - {}", role.getId(), role.getDescription());

        User user = userMapper.requestToUser(userRequestDto);
        user.setPassword(passwordEncoder.encode(userRequestDto.password()));
        user.setRole(role);

        log.info("About to save user with role: {}", user.getRole().getId());
        User savedUser = userRepository.save(user);
        log.info("User saved successfully with id: {}", savedUser.getId());

        return userMapper.userToUserResponseDto(savedUser);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return userMapper.userToUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        log.info("Updating user with id: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

        userMapper.updateUserFromDto(userRequestDto, existingUser);

        if (userRequestDto.password() != null && !userRequestDto.password().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userRequestDto.password()));
        }

        if (userRequestDto.roleId() != null && !userRequestDto.roleId().equals(existingUser.getRole().getId())) {
            Role role = roleRepository.findById(userRequestDto.roleId())
                    .orElseThrow(() -> new RuntimeException("Role with id " + userRequestDto.roleId() + " not found"));
            existingUser.setRole(role);
        }

        User savedUser = userRepository.save(existingUser);

        return userMapper.userToUserResponseDto(savedUser);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting user with id: {}", id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else throw new UserNotFoundException("User not found with id: " + id);
    }

    @Override
    public User findByUsername(String username) {
        log.info("Fetching user with username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    @Transactional
    public User createUser(String username, String password, String roleId) {
        log.info("Creating new user with username: {} and role: {}", username, roleId);

        if (existsByUsername(username)) {
            throw new RuntimeException("User with username " + username + " already exists");
        }

        log.info("Looking for role with id: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role with id " + roleId + " not found"));
        log.info("Found role: {} - {}", role.getId(), role.getDescription());

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        log.info("About to save user with role: {}", user.getRole().getId());
        User savedUser = userRepository.save(user);
        log.info("User saved successfully with id: {}", savedUser.getId());

        return savedUser;
    }
}


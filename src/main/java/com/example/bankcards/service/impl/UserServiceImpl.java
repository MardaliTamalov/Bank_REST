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

/**
 * Реализация сервиса для управления пользователями.
 * <p>
 * Содержит бизнес-логику создания, получения, обновления и удаления пользователей,
 * а также поиска по имени пользователя. Использует {@link UserRepository},
 * {@link RoleRepository}, {@link UserMapper} и {@link PasswordEncoder}.
 * </p>
 *
 * <p>Основные функции:</p>
 * <ul>
 *     <li>Создание нового пользователя с указанием роли</li>
 *     <li>Получение пользователя по ID или имени</li>
 *     <li>Обновление данных пользователя (роль, пароль и пр.)</li>
 *     <li>Удаление пользователя</li>
 *     <li>Проверка существования пользователя по имени</li>
 * </ul>
 *
 * @author …
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Создает нового пользователя на основе {@link UserRequestDto}.
     * <p>
     * Пароль автоматически шифруется, а роль загружается из репозитория.
     * </p>
     *
     * @param userRequestDto данные для создания пользователя
     * @return {@link UserResponseDto} с информацией о созданном пользователе
     * @throws RuntimeException если пользователь с таким username уже существует
     *                          или если указанная роль не найдена
     */
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

    /**
     * Находит пользователя по его ID.
     *
     * @param id идентификатор пользователя
     * @return {@link UserResponseDto} с информацией о пользователе
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public UserResponseDto getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return userMapper.userToUserResponseDto(user);
    }

    /**
     * Обновляет данные существующего пользователя.
     * <p>
     * Обновляются поля username, пароль (если указан),
     * а также роль (если указана и отличается от текущей).
     * </p>
     *
     * @param id             идентификатор пользователя
     * @param userRequestDto новые данные пользователя
     * @return {@link UserResponseDto} с обновленной информацией
     * @throws UserNotFoundException если пользователь с данным ID не найден
     * @throws RuntimeException      если указанная роль не найдена
     */
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

    /**
     * Удаляет пользователя по его ID.
     *
     * @param id идентификатор пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public void deleteById(Long id) {
        log.info("Deleting user with id: {}", id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else throw new UserNotFoundException("User not found with id: " + id);
    }

    /**
     * Ищет пользователя по имени.
     *
     * @param username имя пользователя
     * @return {@link User} найденный пользователь
     * @throws UserNotFoundException если пользователь с таким именем не найден
     */
    @Override
    public User findByUsername(String username) {
        log.info("Fetching user with username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    /**
     * Проверяет, существует ли пользователь с указанным username.
     *
     * @param username имя пользователя
     * @return {@code true}, если пользователь существует, иначе {@code false}
     */
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Создает нового пользователя с указанным именем, паролем и ролью.
     * <p>
     * В отличие от {@link #createUser(UserRequestDto)}, принимает простые аргументы.
     * </p>
     *
     * @param username имя пользователя
     * @param password пароль (будет зашифрован)
     * @param roleId   идентификатор роли
     * @return созданный {@link User}
     * @throws RuntimeException если пользователь с таким username уже существует
     *                          или если указанная роль не найдена
     */
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


package com.example.bankcards.test;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initData() {
        // 1. Инициализация ролей
        initRoles();

        // 2. Сброс паролей тестовых пользователей
        resetTestUsersPasswords();

        // 3. Вывод информации о пользователях (для проверки)
        printUsersInfo();
    }

    private void initRoles() {
        List<String> roleIds = List.of("ADMIN", "USER");

        roleIds.forEach(roleId -> {
            if (!roleRepository.existsById(roleId)) {
                Role role = new Role();
                role.setId(roleId);
                role.setDescription(roleId.equals("ADMIN")
                        ? "Administrator role with full access"
                        : "Standard user role with basic permissions");
                roleRepository.save(role);
                System.out.println("Created role: " + roleId);
            }
        });
    }

    private void resetTestUsersPasswords() {
        // Тестовые пользователи и их новые пароли
        Map<String, String> usersToReset = Map.of(
                "admin@bank.com", "Admin@12345",
                "user@bank.com", "User@12345",
                "newuser@example.com", "NewUser@123"
        );

        usersToReset.forEach((username, newPassword) -> {
            Optional<User> userOpt = userRepository.findByUsername(username);

            userOpt.ifPresentOrElse(
                    user -> {
                        if (!passwordEncoder.matches(newPassword, user.getPassword())) {
                            user.setPassword(passwordEncoder.encode(newPassword));
                            userRepository.save(user);
                            System.out.printf("Password reset for %s to: %s%n", username, newPassword);
                        } else {
                            System.out.printf("Password for %s already matches the new value%n", username);
                        }
                    },
                    () -> System.out.printf("User not found: %s%n", username)
            );
        });
    }

    private void printUsersInfo() {
        System.out.println("\nCurrent users in database:");
        System.out.println("=========================");

        userRepository.findAll().forEach(user -> {
            System.out.println("\nUsername: " + user.getUsername());
            System.out.println("Role: " + user.getRole().getId());
            System.out.println("Password hash: " + user.getPassword());

            // Проверка паролей
            System.out.println("Password verification:");
            System.out.println("  - 'Admin@12345': " +
                    passwordEncoder.matches("Admin@12345", user.getPassword()));
            System.out.println("  - 'User@12345': " +
                    passwordEncoder.matches("User@12345", user.getPassword()));
            System.out.println("  - 'NewUser@123': " +
                    passwordEncoder.matches("NewUser@123", user.getPassword()));
        });

        System.out.println("\n=========================");
    }

    // Дополнительный метод для создания пользователей, если их нет
    @Transactional
    public void createTestUsersIfNotExist() {
        Role adminRole = roleRepository.findById("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

        Role userRole = roleRepository.findById("USER")
                .orElseThrow(() -> new IllegalStateException("USER role not found"));

        createUserIfNotExists("admin@bank.com", "Admin@12345", adminRole);
        createUserIfNotExists("user@bank.com", "User@12345", userRole);
        createUserIfNotExists("newuser@example.com", "NewUser@123", userRole);
    }

    private void createUserIfNotExists(String username, String password, Role role) {
        if (userRepository.findByUsername(username) == null) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            userRepository.save(user);
            System.out.println("Created user: " + username);
        }
    }
}
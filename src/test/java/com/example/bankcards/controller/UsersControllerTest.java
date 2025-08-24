package com.example.bankcards.controller;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, LiquibaseAutoConfiguration.class})
@WithMockUser(roles = {"ADMIN"})

public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CardRepository cardRepository;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    private UserRequestDto request = new UserRequestDto("testuser", "password123", "ADMIN");
    private UserResponseDto response = new UserResponseDto("testuser", "password123", "ADMIN");

    @Test
    @DisplayName("POST /api/users - добавление нового пользователя")
    void addUserShouldReturnCreatedUser() throws Exception {

        Mockito.when(userService.createUser(any(UserRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roleId").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - получение пользователя по ID")
    void getUserShouldReturnUser() throws Exception {

        Mockito.when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roleId").value("ADMIN"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - обновление пользователя")
    void updateUserShouldReturnUpdatedUser() throws Exception {
        UserRequestDto request = new UserRequestDto("newuser", "newpass", "USER");
        UserResponseDto response = new UserResponseDto("newuser", "newpass", "USER");

        Mockito.when(userService.updateUser(eq(1L), any(UserRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.roleId").value("USER"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - удаление пользователя")
    void deleteUserShouldReturnOk() throws Exception {
        Mockito.doNothing().when(userService).deleteById(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}

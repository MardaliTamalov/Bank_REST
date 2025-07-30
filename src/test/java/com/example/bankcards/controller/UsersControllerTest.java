package com.example.bankcards.controller;

import com.example.bankcards.controller.impl.UsersControllerImpl;
import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UsersControllerImpl.class)
@Import(UsersControllerTest.MockedServiceConfig.class)
@WithMockUser(roles = "ADMIN")
@ActiveProfiles("test")
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @TestConfiguration
    static class MockedServiceConfig {

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public JwtTokenProvider jwtTokenProvider() {
            return Mockito.mock(JwtTokenProvider.class);
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return Mockito.mock(UserDetailsService.class);
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider provider,
                                                               UserDetailsService userDetailsService) {
            return new JwtAuthenticationFilter(provider, userDetailsService);
        }
    }


    @Test
    @DisplayName("POST /api/users - add new user")
    void addUser_shouldReturnCreatedUser() throws Exception {
        UserRequestDto request = new UserRequestDto("testuser", "password123", "ADMIN");
        UserResponseDto response = new UserResponseDto("testuser", "password123", "ADMIN");

        Mockito.when(userService.createUser(any(UserRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roleId").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - get user by id")
    void getUser_shouldReturnUser() throws Exception {
        UserResponseDto response = new UserResponseDto("testuser", "password123", "ADMIN");

        Mockito.when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roleId").value("ADMIN"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - update user")
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserRequestDto request = new UserRequestDto("newuser", "newpass", "USER");
        UserResponseDto response = new UserResponseDto("newuser", "newpass", "USER");

        Mockito.when(userService.updateUser(eq(1L), any(UserRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.roleId").value("USER"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - delete user")
    void deleteUser_shouldReturnOk() throws Exception {
        Mockito.doNothing().when(userService).deleteById(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk());
    }
}

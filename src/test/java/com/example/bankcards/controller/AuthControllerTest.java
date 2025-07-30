package com.example.bankcards.controller;

import com.example.bankcards.controller.impl.AuthControllerImpl;
import com.example.bankcards.dto.AuthRequestDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WebMvcTest(AuthControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserService userService;

    @Test
    void authenticateUserShouldReturnJwtTokenWhenCredentialsValid() throws Exception {

        AuthRequestDto request = new AuthRequestDto("testuser", "password123");
        User user = new User();
        user.setUsername("testuser");
        Role role = new Role();
        role.setId("USER");
        user.setRole(role);

        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        Mockito.when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt.token.123");
        Mockito.when(userService.findByUsername("testuser")).thenReturn(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.123"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roleId").value("USER"));
    }

    @Test
    void authenticateUserShouldReturnUnauthorizedWhenCredentialsInvalid() throws Exception {

        AuthRequestDto request = new AuthRequestDto("testuser", "wrongpassword");

        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerUserShouldReturnJwtTokenWhenRegistrationSuccessful() throws Exception {

        AuthRequestDto request = new AuthRequestDto("newuser", "password123");
        User newUser = new User();
        newUser.setUsername("newuser");
        Role role = new Role();
        role.setId("USER");
        newUser.setRole(role);

        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(userService.existsByUsername("newuser")).thenReturn(false);
        Mockito.when(userService.createUser("newuser", "password123", "USER")).thenReturn(newUser);
        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        Mockito.when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt.token.123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.123"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.roleId").value("USER"));
    }

    @Test
    void registerUserShouldReturnBadRequestWhenUsernameExists() throws Exception {

        AuthRequestDto request = new AuthRequestDto("existinguser", "password123");

        Mockito.when(userService.existsByUsername("existinguser")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "currentuser", roles = {"USER"})
    void getCurrentUserShouldReturnUserInfo_WhenAuthenticated() throws Exception {

        User user = new User();
        user.setUsername("currentuser");
        Role role = new Role();
        role.setId("USER");
        user.setRole(role);

        Mockito.when(userService.findByUsername("currentuser")).thenReturn(user);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("currentuser"))
                .andExpect(jsonPath("$.roleId").value("USER"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void getCurrentUserShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
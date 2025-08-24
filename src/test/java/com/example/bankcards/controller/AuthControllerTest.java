package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequestDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.UserService;
import com.example.bankcards.service.impl.TransactionServiceImpl;
import com.example.bankcards.test.DataInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, LiquibaseAutoConfiguration.class})
class AuthControllerTest {
   @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private CardRepository cardRepository;

    @MockitoBean
    private TransactionServiceImpl transactionService;

    @MockitoBean
    private DataInitializer dataInitializer;

    private AuthRequestDto validAuthRequest;
    private AuthRequestDto invalidAuthRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        validAuthRequest = new AuthRequestDto("user@example.com", "Password123!");
        invalidAuthRequest = new AuthRequestDto("", "");

        testUser = new User();
        testUser.setUsername("user@example.com");
        testUser.setRole(new Role("1", "USER"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Успешная аутентификация с валидными учетными данными")
    void authenticateUserWithValidCredentialsShouldReturnTokenAndUserDetails() throws Exception {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                validAuthRequest.getUsername(),
                validAuthRequest.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        Mockito.when(jwtTokenProvider.generateToken(any(Authentication.class)))
                .thenReturn("eyJhbGciOiJIUzI1NiIs...");
        Mockito.when(userService.findByUsername(anyString()))
                .thenReturn(testUser);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiIs..."))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("1"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Неудачная аутентификация с неверными учетными данными")
    void authenticateUserWithInvalidCredentialsShouldReturnUnauthorized() throws Exception {

        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/auth/login - Ошибка валидации с пустыми учетными данными")
    void authenticateUserWithBlankCredentialsShouldReturnBadRequest() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - Успешная регистрация нового пользователя")
    void registerUserWithNewUserShouldReturnTokenAndUserDetails() throws Exception {

        AuthRequestDto registerRequest = new AuthRequestDto("newuser@example.com", "Password123!");
        User newUser = new User();
        newUser.setUsername("newuser@example.com");
        newUser.setRole(new Role("1", "USER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Mockito.when(userService.existsByUsername(anyString())).thenReturn(false);
        Mockito.when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(newUser);
        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        Mockito.when(jwtTokenProvider.generateToken(any(Authentication.class)))
                .thenReturn("eyJhbGciOiJIUzI1NiIs...");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiIs..."))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("newuser@example.com"))
                .andExpect(jsonPath("$.role").value("1"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Ошибка регистрации с существующим email")
    void registerUserWithExistingUsernameShouldReturnBadRequest() throws Exception {

        Mockito.when(userService.existsByUsername(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - Ошибка валидации с пустыми учетными данными")
    void registerUserWithBlankCredentialsShouldReturnBadRequest() throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Ошибка валидации с пустым телом запроса")
    void authenticateUserWithNullRequestBodyShouldReturnBadRequest() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - Ошибка валидации с пустым телом запроса")
    void registerUserWithNullRequestBodyShouldReturnBadRequest() throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - Ошибка валидации с отсутствующим username")
    void authenticateUserWithMissingUsernameShouldReturnBadRequest() throws Exception {

        String requestWithMissingUsername = """
                {
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestWithMissingUsername))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - Ошибка валидации с отсутствующим password")
    void authenticateUserWithMissingPasswordShouldReturnBadRequest() throws Exception {

        String requestWithMissingPassword = """
                {
                    "username": "user@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestWithMissingPassword))
                .andExpect(status().isBadRequest());
    }
}
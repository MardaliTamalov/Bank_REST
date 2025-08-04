package com.example.bankcards.controller;

import com.example.bankcards.controller.impl.AuthControllerImpl;
import com.example.bankcards.dto.AuthRequestDto;
import com.example.bankcards.dto.AuthResponseDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthControllerImpl authController;

    @Autowired
    private MockMvc mockMvc;
    private AuthRequestDto validRequest;
    private User testUser;

    @BeforeEach

    void setUp() {
        validRequest = new AuthRequestDto("testUser", "password");

        Role userRole = new Role("USER", "Regular user");
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setId(1L);
        testUser.setRole(userRole);
    }

    @Test
    void authenticateUser(){

      //  mockMvc.perform(post("/api/auth/login")).andExpect(status.isOk());
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsTokenAndUserInfo() {



        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("testToken");
        when(userService.findByUsername("testUser")).thenReturn(testUser);

        ResponseEntity<AuthResponseDto> response = authController.authenticateUser(validRequest);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testToken", response.getBody().getToken());
        assertEquals("testUser", response.getBody().getUsername());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("testUser", "password"));
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    void registerUser_NewUser_ReturnsTokenAndUserInfo() {

        when(userService.existsByUsername("testUser")).thenReturn(false);
        when(userService.createUser("testUser", "password", "USER")).thenReturn(testUser);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("testToken");


        ResponseEntity<AuthResponseDto> response = authController.registerUser(validRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testToken", response.getBody().getToken());
        assertEquals("testUser", response.getBody().getUsername());

        verify(userService).existsByUsername("testUser");
        verify(userService).createUser("testUser", "password", "USER");
    }

    @Test
    void registerUser_ExistingUser_ReturnsBadRequest() {

        when(userService.existsByUsername("testUser")).thenReturn(true);


        ResponseEntity<AuthResponseDto> response = authController.registerUser(validRequest);


        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).createUser(any(), any(), any());
    }

    @Test
    void getCurrentUser_AuthenticatedUser_ReturnsUserInfo() {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userService.findByUsername("testUser")).thenReturn(testUser);


        ResponseEntity<AuthResponseDto> response = authController.getCurrentUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testUser", response.getBody().getUsername());
        assertNull(response.getBody().getToken());
    }

    @Test
    void getCurrentUser_UserNotFound_ThrowsException() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonExistingUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userService.findByUsername("nonExistingUser")).thenThrow(new UsernameNotFoundException("User not found"));

        assertThrows(UsernameNotFoundException.class, () -> authController.getCurrentUser());
    }
}
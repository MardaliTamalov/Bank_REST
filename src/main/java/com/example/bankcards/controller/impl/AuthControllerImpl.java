package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.AuthController;
import com.example.bankcards.dto.AuthRequestDto;
import com.example.bankcards.dto.AuthResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> authenticateUser(@Valid @RequestBody AuthRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        User user = userService.findByUsername(loginRequest.getUsername());

        return ResponseEntity.ok(new AuthResponseDto(jwt, user.getUsername(), user.getRole().getId()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerUser(@Valid @RequestBody AuthRequestDto registerRequest) {
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().build();
        }

        User newUser = userService.createUser(registerRequest.getUsername(), registerRequest.getPassword(), "USER");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getUsername(), registerRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new AuthResponseDto(jwt, newUser.getUsername(), newUser.getRole().getId()));
    }
}

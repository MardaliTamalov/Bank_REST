package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.UsersController;
import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsersControllerImpl implements UsersController {
    private final UserService userService;

    @Override
    @PostMapping()
    public ResponseEntity<UserResponseDto> addUsers(@RequestBody @Valid UserRequestDto userRequestDto) {
        return ok(userService.createUser(userRequestDto));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        return ok(userService.getUserById(id));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id,
                                                      @Valid @RequestBody UserRequestDto requestDto) {
        return ok(userService.updateUser(id, requestDto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ok().build();
    }
}

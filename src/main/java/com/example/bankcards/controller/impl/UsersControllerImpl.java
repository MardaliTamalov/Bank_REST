package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.UsersController;
import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor

public class UsersControllerImpl implements UsersController {
    private final UserService userService;

    @Override
    public ResponseEntity<UserResponseDto> addUsers(@RequestBody @Valid UserRequestDto userRequestDto) {
        return ok(userService.createUser(userRequestDto));
    }

    @Override
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) throws InterruptedException {
        return ok(userService.getUserById(id));
    }

    @Override
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id,
                                                      @Valid @RequestBody UserRequestDto requestDto) {
        return ok(userService.updateUser(id, requestDto));
    }

    @Override
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ok().build();
    }
}

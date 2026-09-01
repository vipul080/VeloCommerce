package com.vipul.ecommerce.controller;

import com.vipul.ecommerce.dto.LoginRequest;
import com.vipul.ecommerce.dto.LoginResponse;
import com.vipul.ecommerce.dto.RegisterRequest;
import com.vipul.ecommerce.dto.UserResponse;
import com.vipul.ecommerce.entity.User;
import com.vipul.ecommerce.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(request);

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        String token = userService.loginUser(request);

        return ResponseEntity.ok(new LoginResponse(token));
    }
}
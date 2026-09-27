package com.vipul.ecommerce.service;

import com.vipul.ecommerce.dto.LoginRequest;
import com.vipul.ecommerce.dto.RegisterRequest;
import com.vipul.ecommerce.entity.Role;
import com.vipul.ecommerce.entity.User;
import com.vipul.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;


    @Test
    void registerUser_shouldRegisterUserSuccessfully() {

        RegisterRequest request = new RegisterRequest(
                "Vipul",
                "vipul@example.com",
                "password123"
        );

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encodedPassword");

        User savedUser = new User(
                "Vipul",
                "vipul@example.com",
                "encodedPassword",
                Role.CUSTOMER
        );

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        User result = userService.registerUser(request);

        assertNotNull(result);
        assertEquals("Vipul", result.getName());
        assertEquals("vipul@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(Role.CUSTOMER, result.getRole());

        verify(userRepository).findByEmail("vipul@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }


    @Test
    void registerUser_shouldThrowException_whenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest(
                "Vipul",
                "vipul@example.com",
                "password123"
        );

        User existingUser = new User(
                "Existing User",
                "vipul@example.com",
                "encodedPassword",
                Role.CUSTOMER
        );

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(existingUser));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.registerUser(request)
        );

        assertEquals("Email already registered", exception.getMessage());

        verify(userRepository).findByEmail("vipul@example.com");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void loginUser_shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest(
                "vipul@example.com",
                "password123"
        );

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "encodedPassword",
                Role.CUSTOMER
        );

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )).thenReturn(true);

        when(jwtService.generateTokens(
                user.getEmail(),
                user.getRole().name()
        )).thenReturn("jwt-token");

        String result = userService.loginUser(request);

        assertEquals("jwt-token", result);

        verify(userRepository).findByEmail("vipul@example.com");
        verify(passwordEncoder).matches(
                "password123",
                "encodedPassword"
        );
        verify(jwtService).generateTokens(
                "vipul@example.com",
                "CUSTOMER"
        );
    }


    @Test
    void loginUser_shouldThrowException_whenEmailDoesNotExist() {

        LoginRequest request = new LoginRequest(
                "unknown@example.com",
                "password123"
        );

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.loginUser(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        verify(userRepository).findByEmail("unknown@example.com");

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateTokens(anyString(), anyString());
    }


    @Test
    void loginUser_shouldThrowException_whenPasswordIsWrong() {

        LoginRequest request = new LoginRequest(
                "vipul@example.com",
                "wrongPassword"
        );

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "encodedPassword",
                Role.CUSTOMER
        );

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.loginUser(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        verify(userRepository).findByEmail("vipul@example.com");

        verify(passwordEncoder).matches(
                "wrongPassword",
                "encodedPassword"
        );

        verify(jwtService, never())
                .generateTokens(anyString(), anyString());
    }
}
package com.vipul.ecommerce.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "this-is-a-very-long-secret-key-for-testing-jwt"
        );

        ReflectionTestUtils.setField(
                jwtService,
                "expiration",
                3600000L
        );
    }

    @Test
    void generateTokens_shouldGenerateValidToken() {

        String token = jwtService.generateTokens(
                "vipul@example.com",
                "CUSTOMER"
        );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {

        String token = jwtService.generateTokens(
                "vipul@example.com",
                "CUSTOMER"
        );

        String email = jwtService.extractEmail(token);

        assertEquals("vipul@example.com", email);
    }

    @Test
    void extractEmail_shouldThrowException_whenTokenIsInvalid() {

        String invalidToken = "this.is.not.a.valid.jwt";

        assertThrows(
                JwtException.class,
                () -> jwtService.extractEmail(invalidToken)
        );
    }
}
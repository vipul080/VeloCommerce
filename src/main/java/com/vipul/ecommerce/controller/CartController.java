package com.vipul.ecommerce.controller;

import com.vipul.ecommerce.dto.CartItemRequest;
import com.vipul.ecommerce.dto.CartResponse;
import com.vipul.ecommerce.entity.User;
import com.vipul.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest request) {

        User user = (User) authentication.getPrincipal();

        CartResponse response = cartService.addToCart(
                user.getEmail(),
                request
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        CartResponse response = cartService.getCart(
                user.getEmail()
        );

        return ResponseEntity.ok(response);
    }
}
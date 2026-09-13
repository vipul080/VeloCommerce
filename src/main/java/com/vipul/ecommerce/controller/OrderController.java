package com.vipul.ecommerce.controller;

import com.vipul.ecommerce.dto.OrderResponse;
import com.vipul.ecommerce.entity.User;
import com.vipul.ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        OrderResponse response = orderService.placeOrder(
                user.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        List<OrderResponse> orders =
                orderService.getMyOrders(user.getEmail());

        return ResponseEntity.ok(orders);
    }
}
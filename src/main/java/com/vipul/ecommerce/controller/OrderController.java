package com.vipul.ecommerce.controller;

import com.vipul.ecommerce.dto.OrderResponse;
import com.vipul.ecommerce.dto.UpdateOrderStatusRequest;
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

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        orderService.cancelOrder(orderId, user.getEmail());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(orderId, request)
        );
    }
}
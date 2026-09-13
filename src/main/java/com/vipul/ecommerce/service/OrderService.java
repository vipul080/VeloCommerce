package com.vipul.ecommerce.service;

import com.vipul.ecommerce.dto.OrderItemResponse;
import com.vipul.ecommerce.dto.OrderResponse;
import com.vipul.ecommerce.entity.*;
import com.vipul.ecommerce.exception.CartNotFoundException;
import com.vipul.ecommerce.exception.UserNotFoundException;
import com.vipul.ecommerce.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(
            OrderRepository orderRepository, OrderItemRepository orderItemRepository,
            UserRepository userRepository, CartRepository cartRepository, CartItemRepository cartItemRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public List<OrderResponse> getMyOrders(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Order> orders = orderRepository.findByUser(user);

        return orders.stream()
                .map(this::convertToResponse)
                .toList();
    }

    private OrderResponse convertToResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::convertItemToResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }

    private OrderItemResponse convertItemToResponse(OrderItem item) {

        BigDecimal subtotal = item.getPrice()
                .multiply(
                        BigDecimal.valueOf(item.getQuantity())
                );

        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPrice(),
                subtotal
        );
    }

    public OrderResponse placeOrder(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() > product.getStock()) {
                throw new RuntimeException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }
        }

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            product.setStock(
                    product.getStock() - cartItem.getQuantity()
            );
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            BigDecimal subtotal = product.getPrice()
                    .multiply(
                            BigDecimal.valueOf(cartItem.getQuantity())
                    );

            totalAmount = totalAmount.add(subtotal);
        }

        Order order = new Order(
                user,
                OrderStatus.CONFIRMED,
                totalAmount,
                LocalDateTime.now()
        );

        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem(
                    savedOrder,
                    product,
                    cartItem.getQuantity(),
                    product.getPrice()
            );

            orderItemRepository.save(orderItem);
        }

        cart.getItems().clear();
        cartRepository.save(cart);


        return convertToResponse(savedOrder);
    }


}
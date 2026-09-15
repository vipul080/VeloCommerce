package com.vipul.ecommerce.service;

import com.vipul.ecommerce.dto.OrderResponse;
import com.vipul.ecommerce.entity.*;
import com.vipul.ecommerce.exception.CartNotFoundException;
import com.vipul.ecommerce.exception.UserNotFoundException;
import com.vipul.ecommerce.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_shouldCreateOrder_whenCartHasItemsAndStockIsAvailable() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                Role.CUSTOMER
        );

        Product product = new Product(
                "Mechanical Keyboard",
                "RGB mechanical keyboard",
                new BigDecimal("2999.99"),
                10,
                "Electronics",
                null,
                null
        );

        Cart cart = new Cart(user);

        CartItem cartItem = new CartItem(
                cart,
                product,
                2
        );

        cart.getItems().add(cartItem);

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderItemRepository.save(any(OrderItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response =
                orderService.placeOrder("vipul@example.com");

        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        assertEquals(
                new BigDecimal("5999.98"),
                response.getTotalAmount()
        );

        assertEquals(8, product.getStock());

        verify(userRepository).findByEmail("vipul@example.com");
        verify(cartRepository).findByUser(user);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(cartRepository).save(cart);
    }

    @Test
    void placeOrder_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> orderService.placeOrder("unknown@example.com")
        );

        verify(userRepository).findByEmail("unknown@example.com");

        verify(cartRepository, never()).findByUser(any(User.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    @Test
    void placeOrder_shouldThrowException_whenCartNotFound() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                Role.CUSTOMER
        );

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> orderService.placeOrder("vipul@example.com")
        );

        verify(userRepository).findByEmail("vipul@example.com");
        verify(cartRepository).findByUser(user);

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    @Test
    void placeOrder_shouldThrowException_whenCartIsEmpty() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                Role.CUSTOMER
        );

        Cart cart = new Cart(user);

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        assertThrows(
                RuntimeException.class,
                () -> orderService.placeOrder("vipul@example.com")
        );

        verify(userRepository).findByEmail("vipul@example.com");
        verify(cartRepository).findByUser(user);

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    @Test
    void placeOrder_shouldThrowException_whenStockIsInsufficient() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                Role.CUSTOMER
        );

        Product product = new Product(
                "Mechanical Keyboard",
                "RGB mechanical keyboard",
                new BigDecimal("2999.99"),
                2,
                "Electronics",
                null,
                null
        );

        Cart cart = new Cart(user);

        CartItem cartItem = new CartItem(
                cart,
                product,
                5
        );

        cart.getItems().add(cartItem);

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        assertThrows(
                RuntimeException.class,
                () -> orderService.placeOrder("vipul@example.com")
        );

        assertEquals(2, product.getStock());

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }
}
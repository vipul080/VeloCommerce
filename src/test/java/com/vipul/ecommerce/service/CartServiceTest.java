package com.vipul.ecommerce.service;

import com.vipul.ecommerce.dto.CartItemRequest;
import com.vipul.ecommerce.dto.CartResponse;
import com.vipul.ecommerce.entity.Cart;
import com.vipul.ecommerce.entity.CartItem;
import com.vipul.ecommerce.entity.Product;
import com.vipul.ecommerce.entity.User;
import com.vipul.ecommerce.repository.CartItemRepository;
import com.vipul.ecommerce.repository.CartRepository;
import com.vipul.ecommerce.repository.ProductRepository;
import com.vipul.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;


    @Test
    void addToCart_shouldAddNewItem() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                null
        );

        Product product = new Product(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2000.00"),
                10,
                "Electronics",
                null,
                null
        );

        Cart cart = new Cart(user);

        CartItemRequest request = new CartItemRequest(1L, 2);

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.empty());

        CartResponse result =
                cartService.addToCart("vipul@example.com", request);

        assertNotNull(result);

        verify(cartItemRepository).save(any(CartItem.class));
    }


    @Test
    void addToCart_shouldIncreaseQuantity_whenItemAlreadyExists() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                null
        );

        Product product = new Product(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2000.00"),
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

        CartItemRequest request = new CartItemRequest(1L, 3);

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(cartItem));

        CartResponse result =
                cartService.addToCart("vipul@example.com", request);

        assertNotNull(result);
        assertEquals(5, cartItem.getQuantity());

        verify(cartItemRepository).save(cartItem);
    }


    @Test
    void getCart_shouldReturnCartWithCorrectTotal() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                null
        );

        Product product = new Product(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2000.00"),
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

        cart.setItems(new ArrayList<>());
        cart.getItems().add(cartItem);

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        CartResponse result =
                cartService.getCart("vipul@example.com");

        assertNotNull(result);
        assertEquals(
                new BigDecimal("4000.00"),
                result.getTotal()
        );

        assertEquals(1, result.getItems().size());
    }


    @Test
    void getCart_shouldCreateCart_whenCartDoesNotExist() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                null
        );

        Cart newCart = new Cart(user);

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.empty());

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(newCart);

        CartResponse result =
                cartService.getCart("vipul@example.com");

        assertNotNull(result);

        verify(cartRepository).save(any(Cart.class));
    }


    @Test
    void updateCartItem_shouldUpdateQuantity() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                null
        );

        Product product = new Product(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2000.00"),
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

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(cartItem));

        CartResponse result =
                cartService.updateCartItem(
                        "vipul@example.com",
                        1L,
                        5
                );

        assertNotNull(result);
        assertEquals(5, cartItem.getQuantity());

        verify(cartItemRepository).save(cartItem);
    }


    @Test
    void removeFromCart_shouldDeleteItem() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                null
        );

        Product product = new Product(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2000.00"),
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

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(cartItem));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        cartService.removeFromCart(
                "vipul@example.com",
                1L
        );

        verify(cartItemRepository).delete(cartItem);
    }


    @Test
    void addToCart_shouldThrowException_whenUserDoesNotExist() {

        CartItemRequest request = new CartItemRequest(1L, 2);

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(
                        "unknown@example.com",
                        request
                )
        );

        assertEquals("User not found", exception.getMessage());

        verify(productRepository, never()).findById(anyLong());
    }


    @Test
    void addToCart_shouldThrowException_whenProductDoesNotExist() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                null
        );

        CartItemRequest request = new CartItemRequest(1L, 2);

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(
                        "vipul@example.com",
                        request
                )
        );

        assertEquals("Product not found", exception.getMessage());
    }
}
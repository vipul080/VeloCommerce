package com.vipul.ecommerce.controller;

import com.vipul.ecommerce.dto.CartItemRequest;
import com.vipul.ecommerce.dto.CartItemResponse;
import com.vipul.ecommerce.dto.CartResponse;
import com.vipul.ecommerce.entity.User;
import com.vipul.ecommerce.repository.UserRepository;
import com.vipul.ecommerce.service.CartService;
import com.vipul.ecommerce.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CacheManager cacheManager;


    private User createUser() {

        User user = new User();

        user.setEmail("vipul@example.com");

        return user;
    }


    private Authentication createAuthentication(User user) {

        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of()
        );
    }


    private CartResponse createCartResponse() {

        CartItemResponse item = new CartItemResponse(
                1L,
                "Mechanical Keyboard",
                new BigDecimal("2999.99"),
                2,
                new BigDecimal("5999.98")
        );

        return new CartResponse(
                1L,
                List.of(item),
                new BigDecimal("5999.98")
        );
    }


    @Test
    void addToCart_shouldReturnCart() throws Exception {

        User user = createUser();

        CartResponse response = createCartResponse();

        when(cartService.addToCart(
                eq("vipul@example.com"),
                any(CartItemRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/cart/items")
                                .with(authentication(
                                        createAuthentication(user)
                                ))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "productId": 1,
                                            "quantity": 2
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId")
                        .value(1))
                .andExpect(jsonPath("$.items[0].productId")
                        .value(1))
                .andExpect(jsonPath("$.items[0].productName")
                        .value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.items[0].quantity")
                        .value(2))
                .andExpect(jsonPath("$.items[0].subtotal")
                        .value(5999.98))
                .andExpect(jsonPath("$.total")
                        .value(5999.98));

        verify(cartService).addToCart(
                eq("vipul@example.com"),
                any(CartItemRequest.class)
        );
    }


    @Test
    void getCart_shouldReturnCart() throws Exception {

        User user = createUser();

        CartResponse response = createCartResponse();

        when(cartService.getCart("vipul@example.com"))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/cart")
                                .with(authentication(
                                        createAuthentication(user)
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId")
                        .value(1))
                .andExpect(jsonPath("$.items.length()")
                        .value(1))
                .andExpect(jsonPath("$.items[0].productName")
                        .value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.total")
                        .value(5999.98));

        verify(cartService)
                .getCart("vipul@example.com");
    }


    @Test
    void updateCartItem_shouldReturnUpdatedCart()
            throws Exception {

        User user = createUser();

        CartResponse response = createCartResponse();

        when(cartService.updateCartItem(
                eq("vipul@example.com"),
                eq(1L),
                eq(3)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/cart/items/1")
                                .with(authentication(
                                        createAuthentication(user)
                                ))
                                .with(csrf())
                                .param("quantity", "3")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId")
                        .value(1))
                .andExpect(jsonPath("$.items[0].productId")
                        .value(1))
                .andExpect(jsonPath("$.total")
                        .value(5999.98));

        verify(cartService).updateCartItem(
                eq("vipul@example.com"),
                eq(1L),
                eq(3)
        );
    }


    @Test
    void removeFromCart_shouldReturnUpdatedCart()
            throws Exception {

        User user = createUser();

        CartResponse response = new CartResponse(
                1L,
                List.of(),
                BigDecimal.ZERO
        );

        when(cartService.removeFromCart(
                "vipul@example.com",
                1L
        )).thenReturn(response);

        mockMvc.perform(
                        delete("/api/cart/items/1")
                                .with(authentication(
                                        createAuthentication(user)
                                ))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId")
                        .value(1))
                .andExpect(jsonPath("$.items.length()")
                        .value(0))
                .andExpect(jsonPath("$.total")
                        .value(0));

        verify(cartService).removeFromCart(
                "vipul@example.com",
                1L
        );
    }
}
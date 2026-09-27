package com.vipul.ecommerce.controller;

import com.vipul.ecommerce.dto.OrderItemResponse;
import com.vipul.ecommerce.dto.OrderResponse;
import com.vipul.ecommerce.dto.UpdateOrderStatusRequest;
import com.vipul.ecommerce.entity.OrderStatus;
import com.vipul.ecommerce.entity.User;
import com.vipul.ecommerce.repository.UserRepository;
import com.vipul.ecommerce.service.JwtService;
import com.vipul.ecommerce.service.OrderService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

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


    private OrderResponse createOrderResponse() {

        OrderItemResponse item = new OrderItemResponse(
                1L,
                "Mechanical Keyboard",
                2,
                new BigDecimal("2999.99"),
                new BigDecimal("5999.98")
        );

        return new OrderResponse(
                1L,
                OrderStatus.CONFIRMED,
                new BigDecimal("5999.98"),
                LocalDateTime.now(),
                List.of(item)
        );
    }


    @Test
    void placeOrder_shouldReturnOrder() throws Exception {

        User user = createUser();

        OrderResponse response = createOrderResponse();

        when(orderService.placeOrder("vipul@example.com"))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/orders")
                                .with(authentication(
                                        createAuthentication(user)
                                ))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId")
                        .value(1))
                .andExpect(jsonPath("$.status")
                        .value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount")
                        .value(5999.98))
                .andExpect(jsonPath("$.items[0].productId")
                        .value(1))
                .andExpect(jsonPath("$.items[0].productName")
                        .value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.items[0].quantity")
                        .value(2))
                .andExpect(jsonPath("$.items[0].price")
                        .value(2999.99))
                .andExpect(jsonPath("$.items[0].subtotal")
                        .value(5999.98));

        verify(orderService)
                .placeOrder("vipul@example.com");
    }


    @Test
    void getMyOrders_shouldReturnOrders() throws Exception {

        User user = createUser();

        OrderResponse response = createOrderResponse();

        when(orderService.getMyOrders("vipul@example.com"))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/orders")
                                .with(authentication(
                                        createAuthentication(user)
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].orderId")
                        .value(1))
                .andExpect(jsonPath("$[0].status")
                        .value("CONFIRMED"))
                .andExpect(jsonPath("$[0].totalAmount")
                        .value(5999.98));

        verify(orderService)
                .getMyOrders("vipul@example.com");
    }


    @Test
    void cancelOrder_shouldReturn204() throws Exception {

        User user = createUser();

        doNothing()
                .when(orderService)
                .cancelOrder(
                        1L,
                        "vipul@example.com"
                );

        mockMvc.perform(
                        delete("/api/orders/1")
                                .with(authentication(
                                        createAuthentication(user)
                                ))
                                .with(csrf())
                )
                .andExpect(status().isNoContent());

        verify(orderService)
                .cancelOrder(
                        1L,
                        "vipul@example.com"
                );
    }


    @Test
    void getAllOrders_shouldReturnOrders_forAdmin()
            throws Exception {

        OrderResponse response = createOrderResponse();

        when(orderService.getAllOrders())
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/orders/admin")
                                .with(authentication(
                                        createAuthentication(
                                                createUser()
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].orderId")
                        .value(1))
                .andExpect(jsonPath("$[0].status")
                        .value("CONFIRMED"));

        verify(orderService)
                .getAllOrders();
    }


    @Test
    void updateOrderStatus_shouldReturnUpdatedOrder_forAdmin()
            throws Exception {

        OrderResponse response = createOrderResponse();

        when(orderService.updateOrderStatus(
                eq(1L),
                any(UpdateOrderStatusRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/orders/1/status")
                                .with(authentication(
                                        createAuthentication(
                                                createUser()
                                        )
                                ))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "status": "CONFIRMED"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId")
                        .value(1))
                .andExpect(jsonPath("$.status")
                        .value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount")
                        .value(5999.98));

        verify(orderService).updateOrderStatus(
                eq(1L),
                any(UpdateOrderStatusRequest.class)
        );
    }
}
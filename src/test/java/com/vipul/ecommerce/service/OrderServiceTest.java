package com.vipul.ecommerce.service;

import com.vipul.ecommerce.dto.OrderResponse;
import com.vipul.ecommerce.dto.UpdateOrderStatusRequest;
import com.vipul.ecommerce.entity.*;
import com.vipul.ecommerce.exception.*;
import com.vipul.ecommerce.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
                CartEmptyException.class,
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
                InsufficientStockException.class,
                () -> orderService.placeOrder("vipul@example.com")
        );

        assertEquals(2, product.getStock());

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    @Test
    void getMyOrders_shouldReturnOrders_whenUserExists() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                Role.CUSTOMER
        );

        Order order = new Order(
                user,
                OrderStatus.CONFIRMED,
                new BigDecimal("5999.98"),
                LocalDateTime.now()
        );

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByUser(user))
                .thenReturn(List.of(order));

        List<OrderResponse> response =
                orderService.getMyOrders("vipul@example.com");

        assertEquals(1, response.size());

        assertEquals(
                OrderStatus.CONFIRMED,
                response.get(0).getStatus()
        );

        assertEquals(
                new BigDecimal("5999.98"),
                response.get(0).getTotalAmount()
        );

        verify(userRepository).findByEmail("vipul@example.com");
        verify(orderRepository).findByUser(user);
    }

    @Test
    void getMyOrders_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> orderService.getMyOrders("unknown@example.com")
        );

        verify(userRepository).findByEmail("unknown@example.com");

        verify(orderRepository, never()).findByUser(any(User.class));
    }

    @Test
    void getMyOrders_shouldReturnMultipleOrders_whenUserHasMultipleOrders() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                Role.CUSTOMER
        );

        Order order1 = new Order(
                user,
                OrderStatus.CONFIRMED,
                new BigDecimal("5999.98"),
                LocalDateTime.now()
        );

        Order order2 = new Order(
                user,
                OrderStatus.SHIPPED,
                new BigDecimal("2999.99"),
                LocalDateTime.now()
        );

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByUser(user))
                .thenReturn(List.of(order1, order2));

        List<OrderResponse> response =
                orderService.getMyOrders("vipul@example.com");

        assertEquals(2, response.size());

        assertEquals(
                new BigDecimal("5999.98"),
                response.get(0).getTotalAmount()
        );

        assertEquals(
                new BigDecimal("2999.99"),
                response.get(1).getTotalAmount()
        );

        assertEquals(
                OrderStatus.CONFIRMED,
                response.get(0).getStatus()
        );

        assertEquals(
                OrderStatus.SHIPPED,
                response.get(1).getStatus()
        );

        verify(orderRepository).findByUser(user);
    }

    @Test
    void placeOrder_shouldClearCart_afterSuccessfulOrder() {

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

        orderService.placeOrder("vipul@example.com");

        assertTrue(cart.getItems().isEmpty());

        verify(cartRepository).save(cart);
    }

    @Test
    void placeOrder_shouldReturnOrderWithItems() {

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

        assertEquals(1, response.getItems().size());

        assertEquals(
                product.getId(),
                response.getItems().get(0).getProductId()
        );

        assertEquals(
                "Mechanical Keyboard",
                response.getItems().get(0).getProductName()
        );

        assertEquals(
                2,
                response.getItems().get(0).getQuantity()
        );

        assertEquals(
                new BigDecimal("2999.99"),
                response.getItems().get(0).getPrice()
        );

        assertEquals(
                new BigDecimal("5999.98"),
                response.getItems().get(0).getSubtotal()
        );
    }

    @Test
    void cancelOrder_shouldCancelOrderAndRestoreStock() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                Role.CUSTOMER
        );
        user.setId(1L);

        Product product = new Product(
                "Mechanical Keyboard",
                "RGB mechanical keyboard",
                new BigDecimal("2999.99"),
                22,
                "Electronics",
                null,
                null
        );

        Order order = new Order(
                user,
                OrderStatus.CONFIRMED,
                new BigDecimal("5999.98"),
                LocalDateTime.now()
        );

        OrderItem orderItem = new OrderItem(
                order,
                product,
                2,
                new BigDecimal("2999.99")
        );

        order.getItems().add(orderItem);

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        orderService.cancelOrder(1L, "vipul@example.com");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        assertEquals(24, product.getStock());

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_shouldThrowException_whenOrderNotFound() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                Role.CUSTOMER
        );

        user.setId(1L);

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder(
                        99L,
                        "vipul@example.com"
                )
        );

        verify(userRepository).findByEmail("vipul@example.com");
        verify(orderRepository).findById(99L);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_shouldThrowException_whenOrderBelongsToAnotherUser() {

        User currentUser = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                Role.CUSTOMER
        );
        currentUser.setId(1L);

        User orderOwner = new User(
                "Other User",
                "other@example.com",
                "password",
                Role.CUSTOMER
        );
        orderOwner.setId(2L);

        Order order = new Order(
                orderOwner,
                OrderStatus.CONFIRMED,
                new BigDecimal("2999.99"),
                LocalDateTime.now()
        );

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(currentUser));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                UnauthorizedOrderException.class,
                () -> orderService.cancelOrder(
                        1L,
                        "vipul@example.com"
                )
        );

        verify(orderRepository, never()).save(any(Order.class));

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void cancelOrder_shouldThrowException_whenOrderIsNotConfirmed() {

        User user = new User(
                "Vipul",
                "vipul@example.com",
                "password",
                Role.CUSTOMER
        );

        user.setId(1L);

        Order order = new Order(
                user,
                OrderStatus.SHIPPED,
                new BigDecimal("2999.99"),
                LocalDateTime.now()
        );

        when(userRepository.findByEmail("vipul@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                OrderCancellationException.class,
                () -> orderService.cancelOrder(
                        1L,
                        "vipul@example.com"
                )
        );

        assertEquals(OrderStatus.SHIPPED, order.getStatus());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getAllOrdersReturnsAllOrders() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Product product = new Product();
        product.setId(1L);
        product.setName("Mechanical Keyboard");

        Order order1 = new Order(
                user,
                OrderStatus.CONFIRMED,
                new BigDecimal("2999.99"),
                LocalDateTime.now()
        );

        OrderItem item1 = new OrderItem(
                order1,
                product,
                1,
                new BigDecimal("2999.99")
        );

        order1.getItems().add(item1);

        Order order2 = new Order(
                user,
                OrderStatus.CANCELLED,
                new BigDecimal("5999.98"),
                LocalDateTime.now()
        );

        OrderItem item2 = new OrderItem(
                order2,
                product,
                2,
                new BigDecimal("2999.99")
        );

        order2.getItems().add(item2);

        when(orderRepository.findAll())
                .thenReturn(List.of(order1, order2));

        List<OrderResponse> result = orderService.getAllOrders();

        assertEquals(2, result.size());

        assertEquals(OrderStatus.CONFIRMED,
                result.get(0).getStatus());

        assertEquals(OrderStatus.CANCELLED,
                result.get(1).getStatus());

        assertEquals(1, result.get(0).getItems().size());
        assertEquals(1, result.get(1).getItems().size());

        verify(orderRepository).findAll();
    }

    @Test
    void updateOrderStatusSuccessfully() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Product product = new Product();
        product.setId(1L);
        product.setName("Mechanical Keyboard");

        Order order = new Order(
                user,
                OrderStatus.CONFIRMED,
                new BigDecimal("2999.99"),
                LocalDateTime.now()
        );

        OrderItem item = new OrderItem(
                order,
                product,
                1,
                new BigDecimal("2999.99")
        );

        order.getItems().add(item);

        UpdateOrderStatusRequest request =
                new UpdateOrderStatusRequest(OrderStatus.SHIPPED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse result =
                orderService.updateOrderStatus(1L, request);

        assertEquals(OrderStatus.SHIPPED, result.getStatus());
        assertEquals(new BigDecimal("2999.99"), result.getTotalAmount());
        assertEquals(1, result.getItems().size());

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatusThrowsExceptionWhenOrderNotFound() {

        UpdateOrderStatusRequest request =
                new UpdateOrderStatusRequest(OrderStatus.SHIPPED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrderStatus(1L, request)
        );

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatusAllowsValidTransition() {

        User user = new User();
        user.setId(1L);

        Order order = new Order(
                user,
                OrderStatus.SHIPPED,
                new BigDecimal("2999.99"),
                LocalDateTime.now()
        );

        UpdateOrderStatusRequest request =
                new UpdateOrderStatusRequest(OrderStatus.DELIVERED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse result =
                orderService.updateOrderStatus(1L, request);

        assertEquals(OrderStatus.DELIVERED, result.getStatus());

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatusRejectsInvalidTransition() {

        User user = new User();
        user.setId(1L);

        Order order = new Order(
                user,
                OrderStatus.DELIVERED,
                new BigDecimal("2999.99"),
                LocalDateTime.now()
        );

        UpdateOrderStatusRequest request =
                new UpdateOrderStatusRequest(OrderStatus.SHIPPED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                InvalidOrderStatusTransitionException.class,
                () -> orderService.updateOrderStatus(1L, request)
        );

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatusAllowsCancellationFromConfirmed() {

        User user = new User();
        user.setId(1L);

        Order order = new Order(
                user,
                OrderStatus.CONFIRMED,
                new BigDecimal("2999.99"),
                LocalDateTime.now()
        );

        UpdateOrderStatusRequest request =
                new UpdateOrderStatusRequest(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse result =
                orderService.updateOrderStatus(1L, request);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());

        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatusCancellationRestoresStock() {

        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("Mechanical Keyboard");
        product.setStock(21);

        Order order = new Order(
                user,
                OrderStatus.CONFIRMED,
                new BigDecimal("5999.98"),
                LocalDateTime.now()
        );

        OrderItem item = new OrderItem(
                order,
                product,
                2,
                new BigDecimal("2999.99")
        );

        order.getItems().add(item);

        UpdateOrderStatusRequest request =
                new UpdateOrderStatusRequest(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse result =
                orderService.updateOrderStatus(1L, request);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(23, product.getStock());

        verify(orderRepository).save(order);
    }
}
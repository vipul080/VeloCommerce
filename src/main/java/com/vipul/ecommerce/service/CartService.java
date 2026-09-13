package com.vipul.ecommerce.service;

import com.vipul.ecommerce.dto.CartItemRequest;
import com.vipul.ecommerce.dto.CartItemResponse;
import com.vipul.ecommerce.dto.CartResponse;
import com.vipul.ecommerce.entity.Cart;
import com.vipul.ecommerce.entity.CartItem;
import com.vipul.ecommerce.entity.Product;
import com.vipul.ecommerce.entity.User;
import com.vipul.ecommerce.exception.CartItemNotFoundException;
import com.vipul.ecommerce.exception.CartNotFoundException;
import com.vipul.ecommerce.exception.ProductNotFoundException;
import com.vipul.ecommerce.exception.UserNotFoundException;
import com.vipul.ecommerce.repository.CartItemRepository;
import com.vipul.ecommerce.repository.CartRepository;
import com.vipul.ecommerce.repository.ProductRepository;
import com.vipul.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public CartResponse addToCart(
            String email,
            CartItemRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem == null) {

            cartItem = new CartItem(
                    cart,
                    product,
                    request.getQuantity()
            );

        } else {

            cartItem.setQuantity(
                    cartItem.getQuantity() + request.getQuantity()
            );
        }

        cartItemRepository.save(cartItem);

        return getCart(email);
    }

    public CartResponse getCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() ->
                        cartRepository.save(new Cart(user)));

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(item -> {

                    Product product = item.getProduct();

                    BigDecimal subtotal =
                            product.getPrice()
                                    .multiply(
                                            BigDecimal.valueOf(
                                                    item.getQuantity()
                                            )
                                    );

                    return new CartItemResponse(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            item.getQuantity(),
                            subtotal
                    );
                })
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                items,
                total
        );
    }

    public CartResponse updateCartItem(
            String email,
            Long productId,
            Integer quantity) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElseThrow(() ->
                        new RuntimeException("Product not found in cart"));

        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);

        return getCart(email);
    }


    public CartResponse removeFromCart(
            String email,
            Long productId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartNotFoundException("Cart not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found"));

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElseThrow(() ->
                        new CartItemNotFoundException("Product not found in cart"));

        cartItemRepository.delete(cartItem);

        return getCart(email);
    }
}
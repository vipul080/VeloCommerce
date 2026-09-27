package com.vipul.ecommerce.repository;

import com.vipul.ecommerce.entity.Cart;
import com.vipul.ecommerce.entity.CartItem;
import com.vipul.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
package com.vipul.ecommerce.repository;

import com.vipul.ecommerce.entity.Cart;
import com.vipul.ecommerce.entity.CartItem;
import com.vipul.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}
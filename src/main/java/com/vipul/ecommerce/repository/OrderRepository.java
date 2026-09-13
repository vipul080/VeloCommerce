package com.vipul.ecommerce.repository;

import com.vipul.ecommerce.entity.Order;
import com.vipul.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}
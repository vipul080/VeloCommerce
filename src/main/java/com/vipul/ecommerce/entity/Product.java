package com.vipul.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version = 0L;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private String category;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Product(String name, String description, BigDecimal price, int stock, String category, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
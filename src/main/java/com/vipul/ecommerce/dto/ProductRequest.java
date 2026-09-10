package com.vipul.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    private Integer stock;

    @NotBlank
    @Size(max = 100)
    private String category;

    public ProductRequest() {
    }

    public ProductRequest(String name, String description, BigDecimal price,
                          Integer stock, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    public void setName(String name){
        this.name=name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setPrice(BigDecimal price){
        this.price = price;
    }

    public void setStock(Integer stock){
        this.stock = stock;
    }

    public void setCategory(String category){
        this.category = category;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public BigDecimal getPrice(){
        return price;
    }

    public Integer getStock(){
        return stock;
    }

    public String getCategory(){
        return category;
    }


}
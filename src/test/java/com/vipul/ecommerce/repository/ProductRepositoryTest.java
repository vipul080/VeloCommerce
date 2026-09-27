package com.vipul.ecommerce.repository;

import com.vipul.ecommerce.EcommerceBackendApplication;
import com.vipul.ecommerce.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EcommerceBackendApplication.class)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void save_shouldPersistProduct() {

        Product product = new Product();
        product.setName("Test Keyboard");
        product.setDescription("Test product");
        product.setPrice(new BigDecimal("1999.99"));
        product.setStock(10);
        product.setCategory("Test Electronics");

        Product savedProduct = productRepository.save(product);

        assertNotNull(savedProduct.getId());
        assertEquals("Test Keyboard", savedProduct.getName());
        assertEquals(new BigDecimal("1999.99"), savedProduct.getPrice());

        productRepository.delete(savedProduct);
    }

    @Test
    void findById_shouldReturnProduct_whenProductExists() {

        Product product = new Product();
        product.setName("Test Mouse");
        product.setDescription("Test product");
        product.setPrice(new BigDecimal("999.99"));
        product.setStock(20);
        product.setCategory("Test Electronics");

        Product savedProduct = productRepository.save(product);

        Optional<Product> result =
                productRepository.findById(savedProduct.getId());

        assertTrue(result.isPresent());
        assertEquals("Test Mouse", result.get().getName());

        productRepository.delete(savedProduct);
    }

    @Test
    void findByCategory_shouldReturnMatchingProducts() {

        String category = "TestCategory-" + System.currentTimeMillis();

        Product product1 = new Product();
        product1.setName("Test Keyboard");
        product1.setDescription("Keyboard");
        product1.setPrice(new BigDecimal("1999.99"));
        product1.setStock(10);
        product1.setCategory(category);

        Product product2 = new Product();
        product2.setName("Test Mouse");
        product2.setDescription("Mouse");
        product2.setPrice(new BigDecimal("999.99"));
        product2.setStock(20);
        product2.setCategory(category);

        Product savedProduct1 = productRepository.save(product1);
        Product savedProduct2 = productRepository.save(product2);

        Page<Product> result =
                productRepository.findByCategory(
                        category,
                        PageRequest.of(0, 10)
                );

        assertEquals(2, result.getTotalElements());

        assertTrue(
                result.getContent()
                        .stream()
                        .allMatch(product ->
                                product.getCategory().equals(category))
        );

        productRepository.deleteAll(
                List.of(savedProduct1, savedProduct2)
        );
    }
}
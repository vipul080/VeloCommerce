package com.vipul.ecommerce.service;

import com.vipul.ecommerce.dto.PageResponse;
import com.vipul.ecommerce.dto.ProductRequest;
import static org.mockito.ArgumentMatchers.any;

import com.vipul.ecommerce.dto.ProductResponse;
import com.vipul.ecommerce.entity.Product;
import com.vipul.ecommerce.exception.ProductNotFoundException;
import com.vipul.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProductById_shouldReturnProduct_whenProductExists() {

        Product product = new Product(
                "Mechanical Keyboard",
                "RGB mechanical keyboard",
                new BigDecimal("2999.99"),
                22,
                "Electronics",
                null,
                null
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        var result = productService.getProductById(1L);

        assertEquals("Mechanical Keyboard", result.getName());
        assertEquals(new BigDecimal("2999.99"), result.getPrice());
        assertEquals(22, result.getStock());

        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_shouldThrowException_whenProductDoesNotExist() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(999L)
        );

        verify(productRepository).findById(999L);
    }
    @Test
    void createProduct_shouldSaveAndReturnProduct() {

        ProductRequest request = new ProductRequest(
                "Wireless Mouse",
                "Ergonomic wireless mouse",
                new BigDecimal("1499.99"),
                50,
                "Electronics"
        );

        Product savedProduct = new Product(
                "Wireless Mouse",
                "Ergonomic wireless mouse",
                new BigDecimal("1499.99"),
                50,
                "Electronics",
                null,
                null
        );

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        var result = productService.createProduct(request);

        assertEquals("Wireless Mouse", result.getName());
        assertEquals(new BigDecimal("1499.99"), result.getPrice());
        assertEquals(50, result.getStock());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldUpdateAndReturnProduct_whenProductExists() {

        ProductRequest request = new ProductRequest(
                "Updated Keyboard",
                "Updated description",
                new BigDecimal("3499.99"),
                30,
                "Gaming"
        );

        Product product = new Product(
                "Mechanical Keyboard",
                "Old description",
                new BigDecimal("2999.99"),
                22,
                "Electronics",
                null,
                null
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product))
                .thenReturn(product);

        var result = productService.updateProduct(1L, request);

        assertEquals("Updated Keyboard", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(new BigDecimal("3499.99"), result.getPrice());
        assertEquals(30, result.getStock());
        assertEquals("Gaming", result.getCategory());

        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_shouldThrowException_whenProductDoesNotExist() {

        ProductRequest request = new ProductRequest(
                "Updated Keyboard",
                "Updated description",
                new BigDecimal("3499.99"),
                30,
                "Gaming"
        );

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(999L, request)
        );

        verify(productRepository).findById(999L);
    }

    @Test
    void deleteProduct_shouldDeleteProduct_whenProductExists() {

        Product product = new Product();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).findById(1L);
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_shouldThrowException_whenProductDoesNotExist() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(999L)
        );

        verify(productRepository).findById(999L);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void getAllProductsWithoutCategory() {

        Pageable pageable = PageRequest.of(0, 2);

        Product product = new Product();
        product.setId(1L);
        product.setName("Mechanical Keyboard");
        product.setPrice(new BigDecimal("2999.99"));
        product.setStock(23);
        product.setCategory("Electronics");

        Page<Product> productPage =
                new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(pageable))
                .thenReturn(productPage);

        PageResponse<ProductResponse> result =
                productService.getAllProducts(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Mechanical Keyboard",
                result.getContent().get(0).getName());

        verify(productRepository).findAll(pageable);
        verify(productRepository, never())
                .findByCategory(anyString(), any(Pageable.class));
    }

    @Test
    void getAllProductsWithCategory() {

        Pageable pageable = PageRequest.of(0, 2);

        Product product = new Product();
        product.setId(1L);
        product.setName("Mechanical Keyboard");
        product.setPrice(new BigDecimal("2999.99"));
        product.setStock(23);
        product.setCategory("Electronics");

        Page<Product> productPage =
                new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findByCategory("Electronics", pageable))
                .thenReturn(productPage);

        PageResponse<ProductResponse> result =
                productService.getAllProducts("Electronics", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Mechanical Keyboard",
                result.getContent().get(0).getName());
        assertEquals("Electronics",
                result.getContent().get(0).getCategory());

        verify(productRepository)
                .findByCategory("Electronics", pageable);

        verify(productRepository, never())
                .findAll(pageable);
    }

    @Test
    void getAllProductsWithBlankCategory() {

        Pageable pageable = PageRequest.of(0, 2);

        Product product = new Product();
        product.setId(1L);
        product.setName("Mechanical Keyboard");
        product.setPrice(new BigDecimal("2999.99"));
        product.setStock(23);
        product.setCategory("Electronics");

        Page<Product> productPage =
                new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(pageable))
                .thenReturn(productPage);

        PageResponse<ProductResponse> result =
                productService.getAllProducts(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Mechanical Keyboard",
                result.getContent().get(0).getName());

        verify(productRepository).findAll(pageable);

        verify(productRepository, never())
                .findByCategory(anyString(), any(Pageable.class));
    }

    @Test
    void getAllProductsReturnsCorrectPaginationMetadata() {

        Pageable pageable = PageRequest.of(1, 2);

        Product product = new Product();
        product.setId(3L);
        product.setName("USB-C Hub");
        product.setPrice(new BigDecimal("1899.99"));
        product.setStock(30);
        product.setCategory("Electronics");

        Page<Product> productPage =
                new PageImpl<>(List.of(product), pageable, 5);

        when(productRepository.findAll(pageable))
                .thenReturn(productPage);

        PageResponse<ProductResponse> result =
                productService.getAllProducts(null, pageable);

        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(1, result.getContent().size());

        verify(productRepository).findAll(pageable);
    }
}
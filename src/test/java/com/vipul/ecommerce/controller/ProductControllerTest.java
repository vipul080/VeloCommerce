package com.vipul.ecommerce.controller;

import com.vipul.ecommerce.dto.PageResponse;
import com.vipul.ecommerce.dto.ProductRequest;
import com.vipul.ecommerce.dto.ProductResponse;
import com.vipul.ecommerce.repository.UserRepository;
import com.vipul.ecommerce.service.JwtService;
import com.vipul.ecommerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@WithMockUser(username = "admin", roles = "ADMIN")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CacheManager cacheManager;


    @Test
    void getProductById_shouldReturnProduct() throws Exception {

        ProductResponse response = new ProductResponse(
                1L,
                "Mechanical Keyboard",
                "RGB mechanical keyboard",
                new BigDecimal("2999.99"),
                20,
                "Electronics",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(productService.getProductById(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/products/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.price")
                        .value(2999.99));

        verify(productService)
                .getProductById(1L);
    }


    @Test
    void getAllProducts_shouldReturnProducts() throws Exception {

        ProductResponse product = new ProductResponse(
                1L,
                "Mechanical Keyboard",
                "RGB mechanical keyboard",
                new BigDecimal("2999.99"),
                20,
                "Electronics",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        PageResponse<ProductResponse> response =
                new PageResponse<>(
                        List.of(product),
                        0,
                        10,
                        1,
                        1
                );

        when(productService.getAllProducts(
                isNull(),
                any(Pageable.class)
        )).thenReturn(response);

        mockMvc.perform(
                        get("/api/products")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id")
                        .value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.content[0].price")
                        .value(2999.99))
                .andExpect(jsonPath("$.totalElements")
                        .value(1))
                .andExpect(jsonPath("$.totalPages")
                        .value(1));

        verify(productService).getAllProducts(
                isNull(),
                any(Pageable.class)
        );
    }


    @Test
    void createProduct_shouldReturnProduct_forAdmin()
            throws Exception {

        ProductRequest request = new ProductRequest(
                "Mechanical Keyboard",
                "RGB mechanical keyboard",
                new BigDecimal("2999.99"),
                20,
                "Electronics"
        );

        ProductResponse response = new ProductResponse(
                1L,
                "Mechanical Keyboard",
                "RGB mechanical keyboard",
                new BigDecimal("2999.99"),
                20,
                "Electronics",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(productService.createProduct(
                any(ProductRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/products")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Mechanical Keyboard",
                                            "description": "RGB mechanical keyboard",
                                            "price": 2999.99,
                                            "stock": 20,
                                            "category": "Electronics"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.name")
                        .value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.price")
                        .value(2999.99))
                .andExpect(jsonPath("$.stock")
                        .value(20))
                .andExpect(jsonPath("$.category")
                        .value("Electronics"));

        verify(productService)
                .createProduct(any(ProductRequest.class));
    }


    @Test
    void updateProduct_shouldReturnUpdatedProduct_forAdmin()
            throws Exception {

        ProductRequest request = new ProductRequest(
                "Updated Keyboard",
                "Updated description",
                new BigDecimal("3499.99"),
                15,
                "Electronics"
        );

        ProductResponse response = new ProductResponse(
                1L,
                "Updated Keyboard",
                "Updated description",
                new BigDecimal("3499.99"),
                15,
                "Electronics",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(productService.updateProduct(
                eq(1L),
                any(ProductRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/products/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Updated Keyboard",
                                            "description": "Updated description",
                                            "price": 3499.99,
                                            "stock": 15,
                                            "category": "Electronics"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.name")
                        .value("Updated Keyboard"))
                .andExpect(jsonPath("$.price")
                        .value(3499.99))
                .andExpect(jsonPath("$.stock")
                        .value(15))
                .andExpect(jsonPath("$.category")
                        .value("Electronics"));

        verify(productService).updateProduct(
                eq(1L),
                any(ProductRequest.class)
        );
    }


    @Test
    void deleteProduct_shouldReturn204_forAdmin()
            throws Exception {

        doNothing()
                .when(productService)
                .deleteProduct(1L);

        mockMvc.perform(
                        delete("/api/products/1")
                                .with(csrf())
                )
                .andExpect(status().isNoContent());

        verify(productService)
                .deleteProduct(1L);
    }
}
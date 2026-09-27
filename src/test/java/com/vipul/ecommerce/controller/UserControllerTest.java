package com.vipul.ecommerce.controller;

import com.vipul.ecommerce.entity.Role;
import com.vipul.ecommerce.entity.User;
import com.vipul.ecommerce.repository.UserRepository;
import com.vipul.ecommerce.service.JwtService;
import com.vipul.ecommerce.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CacheManager cacheManager;


    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Vipul");
        user.setEmail("vipul@example.com");
        user.setRole(Role.CUSTOMER);
        return user;
    }


    @Test
    void register_shouldReturnUser() throws Exception {

        User user = createUser();

        when(userService.registerUser(any()))
                .thenReturn(user);

        mockMvc.perform(
                        post("/api/users/register")
                                .contentType("application/json")
                                .content("""
                                {
                                    "name": "Vipul",
                                    "email": "vipul@example.com",
                                    "password": "password123"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Vipul"))
                .andExpect(jsonPath("$.email").value("vipul@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }


    @Test
    void login_shouldReturnToken() throws Exception {

        when(userService.loginUser(any()))
                .thenReturn("test-jwt-token");

        mockMvc.perform(
                        post("/api/users/login")
                                .contentType("application/json")
                                .content("""
                                {
                                    "email": "vipul@example.com",
                                    "password": "password123"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"));
    }


    @Test
    void adminTest_shouldReturnMessage() throws Exception {

        mockMvc.perform(
                        get("/api/users/admin-test")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("You are an ADMIN"));
    }
}
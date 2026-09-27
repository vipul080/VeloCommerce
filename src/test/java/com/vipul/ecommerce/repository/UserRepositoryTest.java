package com.vipul.ecommerce.repository;

import com.vipul.ecommerce.EcommerceBackendApplication;
import com.vipul.ecommerce.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EcommerceBackendApplication.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {

        String email = "repository-test-" + System.currentTimeMillis() + "@example.com";

        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("password123");

        User savedUser = userRepository.save(user);

        Optional<User> result = userRepository.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(savedUser.getId(), result.get().getId());

        userRepository.delete(savedUser);
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {

        String email = "does-not-exist-" + System.currentTimeMillis() + "@example.com";

        Optional<User> result = userRepository.findByEmail(email);

        assertTrue(result.isEmpty());
    }
}
package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {

        User user1 = new User();
        user1.setName("Test User");
        user1.setEmail("user@gmail.com");
        user1.setPassword("password123");
        user1.setRole(Role.CUSTOMER);

        User user2 = new User();
        user2.setName("Admin User");
        user2.setEmail("admin@gmail.com");
        user2.setPassword("admin123");
        user2.setRole(Role.ADMIN);

        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Test
    void findByEmail_success() {

        Optional<User> result =
                userRepository.findByEmail("user@gmail.com");

        assertTrue(result.isPresent());

        assertEquals(
                "Test User",
                result.get().getName()
        );

        assertEquals(
                "user@gmail.com",
                result.get().getEmail()
        );
    }

    @Test
    void findByEmail_adminUser_success() {

        Optional<User> result =
                userRepository.findByEmail("admin@gmail.com");

        assertTrue(result.isPresent());

        assertEquals(
                "Admin User",
                result.get().getName()
        );

        assertEquals(
                Role.ADMIN,
                result.get().getRole()
        );
    }

    @Test
    void findByEmail_notFound() {

        Optional<User> result =
                userRepository.findByEmail(
                        "unknown@gmail.com"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmail_caseInsensitive() {

        Optional<User> result =
                userRepository.findByEmail(
                        "USER@gmail.com"
                );

        assertTrue(result.isPresent());

        assertEquals(
                "user@gmail.com",
                result.get().getEmail()
        );
    }

    @Test
    void saveUser_success() {

        User user = new User();

        user.setName("New User");
        user.setEmail("newuser@gmail.com");
        user.setPassword("password");
        user.setRole(Role.CUSTOMER);

        User saved =
                userRepository.save(user);

        assertNotNull(saved.getId());

        Optional<User> result =
                userRepository.findById(saved.getId());

        assertTrue(result.isPresent());

        assertEquals(
                "New User",
                result.get().getName()
        );
    }

    @Test
    void findById_success() {

        User saved =
                userRepository.findByEmail("user@gmail.com")
                        .orElseThrow();

        Optional<User> result =
                userRepository.findById(saved.getId());

        assertTrue(result.isPresent());

        assertEquals(
                "user@gmail.com",
                result.get().getEmail()
        );
    }

    @Test
    void deleteUser_success() {

        User saved =
                userRepository.findByEmail("user@gmail.com")
                        .orElseThrow();

        Long id = saved.getId();

        userRepository.deleteById(id);

        assertFalse(
                userRepository.findById(id).isPresent()
        );
    }
}
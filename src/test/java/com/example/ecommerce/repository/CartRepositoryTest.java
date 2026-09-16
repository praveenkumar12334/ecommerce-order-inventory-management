package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    private User createUser(String name, String email) {

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("password123");
        user.setRole(Role.CUSTOMER);

        return userRepository.save(user);
    }

    @Test
    void saveCart_success() {

        User user =
                createUser("Test User", "user@gmail.com");

        Cart cart = new Cart();
        cart.setUser(user);

        Cart saved =
                cartRepository.save(cart);

        assertNotNull(saved.getId());
        assertNotNull(saved.getUser());

        assertEquals(
                user.getId(),
                saved.getUser().getId()
        );
    }

    @Test
    void findByUserId_success() {

        User user =
                createUser("Test User", "user@gmail.com");

        Cart cart = new Cart();
        cart.setUser(user);

        cartRepository.save(cart);

        Optional<Cart> result =
                cartRepository.findByUserId(user.getId());

        assertTrue(result.isPresent());

        assertEquals(
                user.getId(),
                result.get().getUser().getId()
        );
    }

    @Test
    void findByUserId_notFound() {

        Optional<Cart> result =
                cartRepository.findByUserId(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserId_returnsCorrectCart() {

        User user1 =
                createUser("User One", "user1@gmail.com");

        User user2 =
                createUser("User Two", "user2@gmail.com");

        Cart cart1 = new Cart();
        cart1.setUser(user1);

        Cart cart2 = new Cart();
        cart2.setUser(user2);

        Cart savedCart1 =
                cartRepository.save(cart1);

        cartRepository.save(cart2);

        Optional<Cart> result =
                cartRepository.findByUserId(user1.getId());

        assertTrue(result.isPresent());

        assertEquals(
                savedCart1.getId(),
                result.get().getId()
        );

        assertEquals(
                user1.getId(),
                result.get().getUser().getId()
        );
    }

    @Test
    void findById_success() {

        User user =
                createUser("Test User", "user@gmail.com");

        Cart cart = new Cart();
        cart.setUser(user);

        Cart saved =
                cartRepository.save(cart);

        Optional<Cart> result =
                cartRepository.findById(saved.getId());

        assertTrue(result.isPresent());

        assertEquals(
                saved.getId(),
                result.get().getId()
        );
    }

    @Test
    void deleteCart_success() {

        User user =
                createUser("Test User", "user@gmail.com");

        Cart cart = new Cart();
        cart.setUser(user);

        Cart saved =
                cartRepository.save(cart);

        Long id = saved.getId();

        cartRepository.deleteById(id);

        Optional<Cart> result =
                cartRepository.findById(id);

        assertTrue(result.isEmpty());
    }
}
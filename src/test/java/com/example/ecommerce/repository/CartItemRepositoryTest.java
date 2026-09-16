package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
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
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User createUser(String name, String email) {

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("password123");
        user.setRole(Role.CUSTOMER);

        return userRepository.save(user);
    }

    private Product createProduct(String name, double price) {

        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescription("Test product");
        product.setCategory(category);

        return productRepository.save(product);
    }

    @Test
    void saveCartItem_success() {

        User user =
                createUser("Test User", "user@gmail.com");

        Product product =
                createProduct("Laptop", 50000.0);

        Cart cart = new Cart();
        cart.setUser(user);
        cart = cartRepository.save(cart);

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        CartItem saved =
                cartItemRepository.save(cartItem);

        assertNotNull(saved.getId());

        assertEquals(
                2,
                saved.getQuantity()
        );

        assertEquals(
                cart.getId(),
                saved.getCart().getId()
        );

        assertEquals(
                product.getId(),
                saved.getProduct().getId()
        );
    }

    @Test
    void findByCartIdAndProductId_success() {

        User user =
                createUser("Test User", "user@gmail.com");

        Product product =
                createProduct("Laptop", 50000.0);

        Cart cart = new Cart();
        cart.setUser(user);
        cart = cartRepository.save(cart);

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(3);

        cartItemRepository.save(cartItem);

        Optional<CartItem> result =
                cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                3,
                result.get().getQuantity()
        );

        assertEquals(
                cart.getId(),
                result.get().getCart().getId()
        );

        assertEquals(
                product.getId(),
                result.get().getProduct().getId()
        );
    }

    @Test
    void findByCartIdAndProductId_notFound() {

        Optional<CartItem> result =
                cartItemRepository.findByCartIdAndProductId(
                        999999L,
                        999999L
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByCartIdAndProductId_wrongProduct_returnsEmpty() {

        User user =
                createUser("Test User", "user@gmail.com");

        Product product1 =
                createProduct("Laptop", 50000.0);

        Product product2 =
                createProduct("Mouse", 1000.0);

        Cart cart = new Cart();
        cart.setUser(user);
        cart = cartRepository.save(cart);

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product1);
        cartItem.setQuantity(2);

        cartItemRepository.save(cartItem);

        Optional<CartItem> result =
                cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        product2.getId()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByCartIdAndProductId_wrongCart_returnsEmpty() {

        User user1 =
                createUser("User One", "user1@gmail.com");

        User user2 =
                createUser("User Two", "user2@gmail.com");

        Product product =
                createProduct("Laptop", 50000.0);

        Cart cart1 = new Cart();
        cart1.setUser(user1);
        cart1 = cartRepository.save(cart1);

        Cart cart2 = new Cart();
        cart2.setUser(user2);
        cart2 = cartRepository.save(cart2);

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart1);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cartItemRepository.save(cartItem);

        Optional<CartItem> result =
                cartItemRepository.findByCartIdAndProductId(
                        cart2.getId(),
                        product.getId()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteCartItem_success() {

        User user =
                createUser("Test User", "user@gmail.com");

        Product product =
                createProduct("Laptop", 50000.0);

        Cart cart = new Cart();
        cart.setUser(user);
        cart = cartRepository.save(cart);

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);

        CartItem saved =
                cartItemRepository.save(cartItem);

        Long id = saved.getId();

        cartItemRepository.deleteById(id);

        assertTrue(
                cartItemRepository.findById(id).isEmpty()
        );
    }
}
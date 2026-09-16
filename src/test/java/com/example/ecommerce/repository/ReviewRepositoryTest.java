package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Review;
import com.example.ecommerce.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Category createCategory() {

        Category category = new Category();
        category.setName("Electronics");

        return categoryRepository.save(category);
    }

    private Product createProduct(String name) {

        Product product = new Product();
        product.setName(name);
        product.setPrice(50000.0);
        product.setDescription("Test product");
        product.setCategory(createCategory());

        return productRepository.save(product);
    }

    private User createUser(String email) {

        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword(
                new BCryptPasswordEncoder().encode("password")
        );

        return userRepository.save(user);
    }

    private Review createReview(
            User user,
            Product product,
            int rating,
            String comment) {

        Review review = new Review();

        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);

        return reviewRepository.save(review);
    }

    @Test
    void saveReview_success() {

        User user = createUser("user1@gmail.com");
        Product product = createProduct("Laptop");

        Review review = createReview(
                user,
                product,
                5,
                "Excellent product"
        );

        assertNotNull(review.getId());
        assertEquals(5, review.getRating());
        assertEquals("Excellent product", review.getComment());
    }

    @Test
    void findByProductId_success() {

        User user1 = createUser("user1@gmail.com");
        User user2 = createUser("user2@gmail.com");

        Product product = createProduct("Laptop");

        createReview(user1, product, 5, "Excellent");
        createReview(user2, product, 4, "Very good");

        List<Review> result =
                reviewRepository.findByProductId(product.getId());

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(review ->
                                review.getProduct()
                                        .getId()
                                        .equals(product.getId()))
        );
    }

    @Test
    void findByProductId_onlyReturnsRequestedProduct() {

        User user1 = createUser("user1@gmail.com");
        User user2 = createUser("user2@gmail.com");

        Product laptop = createProduct("Laptop");
        Product phone = createProduct("Phone");

        createReview(user1, laptop, 5, "Excellent");
        createReview(user2, phone, 4, "Good");

        List<Review> result =
                reviewRepository.findByProductId(laptop.getId());

        assertEquals(1, result.size());
        assertEquals(
                laptop.getId(),
                result.getFirst().getProduct().getId()
        );
    }

    @Test
    void findByProductId_noResult() {

        Product product = createProduct("Laptop");

        List<Review> result =
                reviewRepository.findByProductId(product.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserId_success() {

        User user = createUser("user@gmail.com");

        Product laptop = createProduct("Laptop");
        Product phone = createProduct("Phone");

        createReview(user, laptop, 5, "Excellent");
        createReview(user, phone, 4, "Good");

        List<Review> result =
                reviewRepository.findByUserId(user.getId());

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(review ->
                                review.getUser()
                                        .getId()
                                        .equals(user.getId()))
        );
    }

    @Test
    void findByUserId_onlyReturnsRequestedUser() {

        User user1 = createUser("user1@gmail.com");
        User user2 = createUser("user2@gmail.com");

        Product product = createProduct("Laptop");

        createReview(user1, product, 5, "Excellent");

        // Different product is needed because the same
        // user-product pair should normally be unique.
        Product product2 = createProduct("Phone");

        createReview(user2, product2, 3, "Average");

        List<Review> result =
                reviewRepository.findByUserId(user1.getId());

        assertEquals(1, result.size());
        assertEquals(
                user1.getId(),
                result.getFirst().getUser().getId()
        );
    }

    @Test
    void findByUserId_noResult() {

        User user = createUser("user@gmail.com");

        List<Review> result =
                reviewRepository.findByUserId(user.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserIdAndProductId_success() {

        User user = createUser("user@gmail.com");
        Product product = createProduct("Laptop");

        Review review =
                createReview(
                        user,
                        product,
                        5,
                        "Excellent product"
                );

        Optional<Review> result =
                reviewRepository.findByUserIdAndProductId(
                        user.getId(),
                        product.getId()
                );

        assertTrue(result.isPresent());
        assertEquals(
                review.getId(),
                result.get().getId()
        );
        assertEquals(
                user.getId(),
                result.get().getUser().getId()
        );
        assertEquals(
                product.getId(),
                result.get().getProduct().getId()
        );
    }

    @Test
    void findByUserIdAndProductId_noResult() {

        User user = createUser("user@gmail.com");
        Product product = createProduct("Laptop");

        Optional<Review> result =
                reviewRepository.findByUserIdAndProductId(
                        user.getId(),
                        product.getId()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_success() {

        User user = createUser("user@gmail.com");
        Product product = createProduct("Laptop");

        Review review =
                createReview(
                        user,
                        product,
                        5,
                        "Excellent"
                );

        Optional<Review> result =
                reviewRepository.findById(review.getId());

        assertTrue(result.isPresent());
        assertEquals(
                review.getId(),
                result.get().getId()
        );
    }

    @Test
    void deleteReview_success() {

        User user = createUser("user@gmail.com");
        Product product = createProduct("Laptop");

        Review review =
                createReview(
                        user,
                        product,
                        5,
                        "Excellent"
                );

        Long id = review.getId();

        reviewRepository.delete(review);

        Optional<Review> result =
                reviewRepository.findById(id);

        assertTrue(result.isEmpty());
    }
}
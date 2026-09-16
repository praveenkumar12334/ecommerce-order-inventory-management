package com.example.ecommerce.service;

import com.example.ecommerce.dto.ReviewRequest;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.AccessDeniedException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.ReviewRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderRepository orderRepository) {

        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public Review addReview(
            Long productId,
            ReviewRequest request,
            String email) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        // Check whether customer purchased the product
        List<Order> orders =
                orderRepository.findCustomerOrdersContainingProduct(
                        email,
                        productId,
                        OrderStatus.DELIVERED
                );

        if (orders.isEmpty()) {
            throw new AccessDeniedException(
                    "You can review only products you purchased"
            );
        }

        // Check duplicate review
        if (reviewRepository
                .findByUserIdAndProductId(
                        user.getId(),
                        productId
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "You have already reviewed this product"
            );
        }

        Review review = new Review();

        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setReviewDate(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public List<Review> getProductReviews(Long productId) {

        return reviewRepository
                .findByProductId(productId);
    }
}
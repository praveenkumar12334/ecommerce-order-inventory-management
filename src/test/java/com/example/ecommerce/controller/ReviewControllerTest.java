package com.example.ecommerce.controller;

import com.example.ecommerce.config.SecurityConfig;
import com.example.ecommerce.dto.ReviewRequest;
import com.example.ecommerce.entity.Review;
import com.example.ecommerce.service.ReviewService;
import com.example.ecommerce.security.CustomUserDetailsService;
import com.example.ecommerce.security.JwtService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@Import(SecurityConfig.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // =========================================================
    // 1. CUSTOMER - ADD REVIEW
    // =========================================================

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCanAddReview() throws Exception {

        Review review = new Review();
        review.setRating(5);
        review.setComment("Excellent product");

        when(reviewService.addReview(
                eq(1L),
                any(ReviewRequest.class),
                eq("user@gmail.com")
        )).thenReturn(review);

        mockMvc.perform(
                        post("/api/reviews/product/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"rating\":5,\"comment\":\"Excellent product\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent product"));

        verify(reviewService).addReview(
                eq(1L),
                any(ReviewRequest.class),
                eq("user@gmail.com")
        );
    }


    // =========================================================
    // 2. ADMIN - ADD REVIEW
    // =========================================================

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanAddReview() throws Exception {

        Review review = new Review();
        review.setRating(4);
        review.setComment("Good product");

        when(reviewService.addReview(
                eq(1L),
                any(ReviewRequest.class),
                eq("admin@gmail.com")
        )).thenReturn(review);

        mockMvc.perform(
                        post("/api/reviews/product/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"rating\":4,\"comment\":\"Good product\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Good product"));

        verify(reviewService).addReview(
                eq(1L),
                any(ReviewRequest.class),
                eq("admin@gmail.com")
        );
    }


    // =========================================================
    // 3. STAFF - ADD REVIEW
    // =========================================================

    @Test
    @WithMockUser(username = "staff@gmail.com", roles = "STAFF")
    void staffCanAddReview() throws Exception {

        Review review = new Review();
        review.setRating(3);
        review.setComment("Average product");

        when(reviewService.addReview(
                eq(1L),
                any(ReviewRequest.class),
                eq("staff@gmail.com")
        )).thenReturn(review);

        mockMvc.perform(
                        post("/api/reviews/product/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"rating\":3,\"comment\":\"Average product\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(3))
                .andExpect(jsonPath("$.comment").value("Average product"));

        verify(reviewService).addReview(
                eq(1L),
                any(ReviewRequest.class),
                eq("staff@gmail.com")
        );
    }


    // =========================================================
    // 4. UNAUTHENTICATED - ADD REVIEW
    // =========================================================

    @Test
    void unauthenticatedUserCannotAddReview() throws Exception {

        mockMvc.perform(
                        post("/api/reviews/product/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"rating\":5,\"comment\":\"Excellent product\"}")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(reviewService);
    }


    // =========================================================
    // 5. CUSTOMER - GET REVIEWS
    // =========================================================

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCanGetProductReviews() throws Exception {

        Review review = new Review();
        review.setRating(5);
        review.setComment("Excellent product");

        when(reviewService.getProductReviews(1L))
                .thenReturn(List.of(review));

        mockMvc.perform(
                        get("/api/reviews/product/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].comment")
                        .value("Excellent product"));

        verify(reviewService).getProductReviews(1L);
    }


    // =========================================================
    // 6. ADMIN - GET REVIEWS
    // =========================================================

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanGetProductReviews() throws Exception {

        Review review = new Review();
        review.setRating(4);
        review.setComment("Good product");

        when(reviewService.getProductReviews(1L))
                .thenReturn(List.of(review));

        mockMvc.perform(
                        get("/api/reviews/product/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value(4))
                .andExpect(jsonPath("$[0].comment")
                        .value("Good product"));

        verify(reviewService).getProductReviews(1L);
    }


    // =========================================================
    // 7. STAFF - GET REVIEWS
    // =========================================================

    @Test
    @WithMockUser(username = "staff@gmail.com", roles = "STAFF")
    void staffCanGetProductReviews() throws Exception {

        Review review = new Review();
        review.setRating(3);
        review.setComment("Average product");

        when(reviewService.getProductReviews(1L))
                .thenReturn(List.of(review));

        mockMvc.perform(
                        get("/api/reviews/product/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value(3))
                .andExpect(jsonPath("$[0].comment")
                        .value("Average product"));

        verify(reviewService).getProductReviews(1L);
    }


    // =========================================================
    // 8. UNAUTHENTICATED - GET REVIEWS
    // =========================================================

    @Test
    void anyoneCanGetProductReviews()
            throws Exception {

        mockMvc.perform(
                        get("/api/reviews/product/1")
                )
                .andExpect(status().isOk());

        verify(reviewService).getProductReviews(1L);
    }


    // =========================================================
    // 9. CUSTOMER - EMPTY REVIEW LIST
    // =========================================================

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerGetsEmptyReviewList() throws Exception {

        when(reviewService.getProductReviews(1L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                        get("/api/reviews/product/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(reviewService).getProductReviews(1L);
    }


    // =========================================================
    // 10. INVALID REVIEW REQUEST
    // =========================================================

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void invalidReviewRequestReturnsBadRequest()
            throws Exception {

        mockMvc.perform(
                        post("/api/reviews/product/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"rating\":0,\"comment\":\"Bad\"}")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }
}
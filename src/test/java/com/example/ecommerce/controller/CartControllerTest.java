package com.example.ecommerce.controller;

import com.example.ecommerce.dto.OrderItemResponse;
import com.example.ecommerce.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ecommerce.config.SecurityConfig;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.security.CustomUserDetailsService;
import com.example.ecommerce.security.JwtService;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // 1. Customer can create cart
    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCanCreateCart() throws Exception {

        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();

        when(userService.findByEmailOrThrow("user@gmail.com"))
                .thenReturn(user);

        when(cartService.createCart(1L))
                .thenReturn(cart);

        mockMvc.perform(
                post("/api/carts/me")
        ).andExpect(status().isOk());

        verify(userService)
                .findByEmailOrThrow("user@gmail.com");

        verify(cartService)
                .createCart(1L);
    }


    // 2. Customer can get cart
    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCanGetCart() throws Exception {

        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();

        when(userService.findByEmailOrThrow("user@gmail.com"))
                .thenReturn(user);

        when(cartService.getCartByUserId(1L))
                .thenReturn(cart);

        mockMvc.perform(
                get("/api/carts/me")
        ).andExpect(status().isOk());

        verify(cartService)
                .getCartByUserId(1L);
    }


    // 3. Customer can add product
    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCanAddProduct() throws Exception {

        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();

        when(userService.findByEmailOrThrow("user@gmail.com"))
                .thenReturn(user);

        when(cartService.addProductToCart(1L, 10L, 2))
                .thenReturn(cart);

        mockMvc.perform(
                post("/api/carts/me/products/10")
                        .param("quantity", "2")
        ).andExpect(status().isOk());

        verify(cartService)
                .addProductToCart(1L, 10L, 2);
    }


    // 4. Customer can update product quantity
    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCanUpdateProduct() throws Exception {

        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();

        when(userService.findByEmailOrThrow("user@gmail.com"))
                .thenReturn(user);

        when(cartService.updateCartItemQuantity(1L, 10L, 5))
                .thenReturn(cart);

        mockMvc.perform(
                put("/api/carts/me/products/10")
                        .param("quantity", "5")
        ).andExpect(status().isOk());

        verify(cartService)
                .updateCartItemQuantity(1L, 10L, 5);
    }


    // 5. Customer can remove product
    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCanRemoveProduct() throws Exception {

        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();

        when(userService.findByEmailOrThrow("user@gmail.com"))
                .thenReturn(user);

        when(cartService.removeProductFromCart(1L, 10L))
                .thenReturn(cart);

        mockMvc.perform(
                delete("/api/carts/me/products/10")
        ).andExpect(status().isOk());

        verify(cartService)
                .removeProductFromCart(1L, 10L);
    }


    // 6. Customer can checkout
    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCanCheckout() throws Exception {

        User user = new User();
        user.setId(1L);

        OrderResponse response = new OrderResponse(
                1L,
                "user@gmail.com",
                "Test Order",
                LocalDateTime.now(),
                OrderStatus.PLACED,
                1000.0,
                List.<OrderItemResponse>of()
        );

        when(userService.findByEmailOrThrow("user@gmail.com"))
                .thenReturn(user);

        when(cartService.checkout(1L, "user@gmail.com",1L))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/carts/me/checkout").param("addressId", "1")
        ).andExpect(status().isOk());

        verify(cartService)
                .checkout(1L, "user@gmail.com",1L);
    }


    // 7. Unauthenticated user cannot create cart
    @Test
    void unauthenticatedUserCannotCreateCart() throws Exception {

        mockMvc.perform(
                post("/api/carts/me")
        ).andExpect(status().isForbidden());

        verify(userService, never())
                .findByEmailOrThrow(anyString());

        verify(cartService, never())
                .createCart(anyLong());
    }


    // 8. Unauthenticated user cannot get cart
    @Test
    void unauthenticatedUserCannotGetCart() throws Exception {

        mockMvc.perform(
                get("/api/carts/me")
        ).andExpect(status().isForbidden());

        verify(userService, never())
                .findByEmailOrThrow(anyString());

        verify(cartService, never())
                .getCartByUserId(anyLong());
    }


    // 9. Unauthenticated user cannot add product
    @Test
    void unauthenticatedUserCannotAddProduct() throws Exception {

        mockMvc.perform(
                post("/api/carts/me/products/10")
                        .param("quantity", "2")
        ).andExpect(status().isForbidden());

        verify(cartService, never())
                .addProductToCart(anyLong(), anyLong(), anyInt());
    }


    // 10. Unauthenticated user cannot checkout
    @Test
    void unauthenticatedUserCannotCheckout() throws Exception {

        mockMvc.perform(
                post("/api/carts/me/checkout")
        ).andExpect(status().isForbidden());

        verify(userService, never())
                .findByEmailOrThrow(anyString());

        verify(cartService, never())
                .checkout(anyLong(), anyString() ,anyLong());
    }
}
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService,
                          UserService userService) {

        this.cartService = cartService;
        this.userService = userService;
    }

    @PostMapping("/me")
    public Cart createCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmailOrThrow(
                userDetails.getUsername()
        );

        return cartService.createCart(user.getId());
    }

    @GetMapping("/me")
    public Cart getCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmailOrThrow(
                userDetails.getUsername()
        );

        return cartService.getCartByUserId(user.getId());
    }

    @PostMapping("/me/products/{productId}")
    public Cart addProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestParam int quantity) {

        User user = userService.findByEmailOrThrow(
                userDetails.getUsername()
        );

        return cartService.addProductToCart(
                user.getId(),
                productId,
                quantity
        );
    }

    @PutMapping("/me/products/{productId}")
    public Cart updateProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestParam int quantity) {

        User user = userService.findByEmailOrThrow(
                userDetails.getUsername()
        );

        return cartService.updateCartItemQuantity(
                user.getId(),
                productId,
                quantity
        );
    }

    @DeleteMapping("/me/products/{productId}")
    public Cart removeProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {

        User user = userService.findByEmailOrThrow(
                userDetails.getUsername()
        );

        return cartService.removeProductFromCart(
                user.getId(),
                productId
        );
    }

    @PostMapping("/me/checkout")
    public OrderResponse checkout(
            @RequestParam Long addressId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmailOrThrow(
                userDetails.getUsername()
        );

        return cartService.checkout(
                user.getId(),
                userDetails.getUsername(),
                addressId
        );
    }
}
package com.example.ecommerce.service;

import com.example.ecommerce.dto.OrderItemRequest;
import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final InventoryRepository inventoryRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository,
                       OrderService orderService,
                       InventoryRepository inventoryRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public Cart createCart(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + userId
                        )
                );

        Cart existingCart = cartRepository
                .findByUserId(userId)
                .orElse(null);

        if (existingCart != null) {
            return existingCart;
        }

        Cart cart = new Cart();
        cart.setUser(user);

        return cartRepository.save(cart);
    }

    public Cart getCartByUserId(Long userId) {

        return cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found for user ID: " + userId
                        )
                );
    }

    @Transactional
    public Cart addProductToCart(Long userId,
                                 Long productId,
                                 int quantity) {

        // 1. Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        // 2. Find or create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));

        // 3. Find product
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with ID: " + productId
                        )
                );

        // 4. Find inventory
        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found for product ID: "
                                        + productId
                        )
                );

        // 5. Find existing cart item
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        productId
                )
                .orElse(null);

        // 6. Calculate final quantity
        int finalQuantity;

        if (cartItem != null) {

            finalQuantity =
                    cartItem.getQuantity() + quantity;

        } else {

            finalQuantity = quantity;
        }

        // 7. Check final quantity against stock
        if (finalQuantity > inventory.getQuantity()) {
            throw new InsufficientStockException(
                    "Only " + inventory.getQuantity()
                            + " units are available"
            );
        }

        // 8. Update existing item
        if (cartItem != null) {

            cartItem.setQuantity(finalQuantity);

        } else {

            // 9. Create new cart item
            cartItem = new CartItem();

            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);

            cart.getCartItems().add(cartItem);
        }

        // 10. Save cart
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateCartItemQuantity(
            Long userId,
            Long productId,
            int quantity) {

        // 1. Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        // 2. Find cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found for user ID: " + userId
                        )
                );

        // 3. Find cart item
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        productId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found in cart"
                        )
                );

        // 4. Find inventory
        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found for product ID: "
                                        + productId
                        )
                );

        // 5. Check requested quantity against stock
        if (quantity > inventory.getQuantity()) {
            throw new InsufficientStockException(
                    "Only " + inventory.getQuantity()
                            + " units are available"
            );
        }

        // 6. Update cart quantity
        cartItem.setQuantity(quantity);

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeProductFromCart(
            Long userId,
            Long productId) {

        // 1. Find cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found for user ID: " + userId
                        )
                );

        // 2. Find cart item
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        productId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found in cart"
                        )
                );

        // 3. Remove item
        cart.getCartItems().remove(cartItem);

        // orphanRemoval = true removes the CartItem
        // from the database.
        return cartRepository.save(cart);
    }

    @Transactional
    public OrderResponse checkout(Long userId,
                                  String loggedInEmail,
                                  Long addressId) {

        // 1. Find cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found"
                        )
                );

        // 2. Check whether cart is empty
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cart is empty"
            );
        }

        // 3. Create order request
        OrderRequest orderRequest = new OrderRequest();

        orderRequest.setCustomerName(
                cart.getUser().getName()
        );

        orderRequest.setCustomerEmail(
                loggedInEmail
        );

        orderRequest.setAddressId(addressId);

        // 4. Convert cart items to order items
        List<OrderItemRequest> items = cart.getCartItems()
                .stream()
                .map(cartItem -> {

                    OrderItemRequest item =
                            new OrderItemRequest();

                    item.setProductId(
                            cartItem.getProduct().getId()
                    );

                    item.setQuantity(
                            cartItem.getQuantity()
                    );

                    return item;
                })
                .toList();

        orderRequest.setItems(items);

        // 5. Create order
        OrderResponse response = orderService.saveOrder(
                orderRequest,
                loggedInEmail
        );

        // 6. Clear cart after successful order creation
        cart.getCartItems().clear();

        // orphanRemoval = true removes the CartItems
        // from the database.
        cartRepository.save(cart);

        return response;
    }
}
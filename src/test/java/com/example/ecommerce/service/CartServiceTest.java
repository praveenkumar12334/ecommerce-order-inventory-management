package com.example.ecommerce.service;

import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private Product product;
    private Inventory inventory;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("user@gmail.com");

        cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setCartItems(new ArrayList<>());

        product = new Product();
        product.setId(100L);
        product.setName("Laptop");
        product.setPrice(50000);

        inventory = new Inventory();
        inventory.setId(200L);
        inventory.setProduct(product);
        inventory.setQuantity(10);
        inventory.setLowStockThreshold(5);

        cartItem = new CartItem();
        cartItem.setId(300L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
    }

    // =========================================================
    // createCart()
    // =========================================================

    @Test
    void createCart_shouldCreateNewCart() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> {
                    Cart saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        Cart result = cartService.createCart(1L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(user, result.getUser());
        assertTrue(result.getCartItems().isEmpty());

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void createCart_shouldReturnExistingCart() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        Cart result = cartService.createCart(1L);

        assertSame(cart, result);

        verify(cartRepository, never())
                .save(any(Cart.class));
    }

    @Test
    void createCart_shouldThrowWhenUserNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.createCart(1L)
        );

        verify(cartRepository, never())
                .save(any(Cart.class));
    }

    // =========================================================
    // getCartByUserId()
    // =========================================================

    @Test
    void getCartByUserId_shouldReturnCart() {

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        Cart result =
                cartService.getCartByUserId(1L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(user, result.getUser());
    }

    @Test
    void getCartByUserId_shouldThrowWhenNotFound() {

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.getCartByUserId(1L)
        );
    }

    // =========================================================
    // addProductToCart()
    // =========================================================

    @Test
    void addProductToCart_shouldAddNewProduct() {

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(100L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.empty());

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result =
                cartService.addProductToCart(
                        1L,
                        100L,
                        2
                );

        assertNotNull(result);
        assertEquals(1, result.getCartItems().size());

        CartItem addedItem =
                result.getCartItems().getFirst();

        assertEquals(product, addedItem.getProduct());
        assertEquals(2, addedItem.getQuantity());
        assertEquals(cart, addedItem.getCart());

        verify(cartRepository).save(cart);
    }

    @Test
    void addProductToCart_shouldIncreaseExistingQuantity() {

        cart.getCartItems().add(cartItem);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(100L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.of(cartItem));

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result =
                cartService.addProductToCart(
                        1L,
                        100L,
                        3
                );

        assertEquals(5, cartItem.getQuantity());

        assertEquals(
                1,
                result.getCartItems().size()
        );

        verify(cartRepository).save(cart);
    }

    @Test
    void addProductToCart_shouldRejectZeroQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addProductToCart(
                        1L,
                        100L,
                        0
                )
        );

        verify(cartRepository, never())
                .save(any(Cart.class));
    }

    @Test
    void addProductToCart_shouldRejectNegativeQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addProductToCart(
                        1L,
                        100L,
                        -1
                )
        );
    }

    @Test
    void addProductToCart_shouldRejectInsufficientStockForNewItem() {

        inventory.setQuantity(2);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(100L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.addProductToCart(
                        1L,
                        100L,
                        3
                )
        );

        verify(cartRepository, never())
                .save(any(Cart.class));
    }

    @Test
    void addProductToCart_shouldRejectInsufficientStockForExistingItem() {

        cartItem.setQuantity(8);
        cart.getCartItems().add(cartItem);

        inventory.setQuantity(10);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(100L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.of(cartItem));

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.addProductToCart(
                        1L,
                        100L,
                        3
                )
        );

        assertEquals(8, cartItem.getQuantity());

        verify(cartRepository, never())
                .save(any(Cart.class));
    }

    @Test
    void addProductToCart_shouldThrowWhenProductNotFound() {

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addProductToCart(
                        1L,
                        100L,
                        2
                )
        );
    }

    @Test
    void addProductToCart_shouldThrowWhenInventoryNotFound() {

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(100L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addProductToCart(
                        1L,
                        100L,
                        2
                )
        );
    }

    // =========================================================
    // updateCartItemQuantity()
    // =========================================================

    @Test
    void updateCartItemQuantity_shouldUpdateSuccessfully() {

        cart.getCartItems().add(cartItem);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.of(cartItem));

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result =
                cartService.updateCartItemQuantity(
                        1L,
                        100L,
                        5
                );

        assertEquals(5, cartItem.getQuantity());
        assertSame(cart, result);

        verify(cartRepository).save(cart);
    }

    @Test
    void updateCartItemQuantity_shouldRejectZeroQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.updateCartItemQuantity(
                        1L,
                        100L,
                        0
                )
        );
    }

    @Test
    void updateCartItemQuantity_shouldRejectNegativeQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.updateCartItemQuantity(
                        1L,
                        100L,
                        -2
                )
        );
    }

    @Test
    void updateCartItemQuantity_shouldThrowWhenCartNotFound() {

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.updateCartItemQuantity(
                        1L,
                        100L,
                        5
                )
        );
    }

    @Test
    void updateCartItemQuantity_shouldThrowWhenItemNotFound() {

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.updateCartItemQuantity(
                        1L,
                        100L,
                        5
                )
        );
    }

    @Test
    void updateCartItemQuantity_shouldRejectInsufficientStock() {

        inventory.setQuantity(3);
        cart.getCartItems().add(cartItem);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.of(cartItem));

        when(inventoryRepository.findByProductId(100L))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.updateCartItemQuantity(
                        1L,
                        100L,
                        5
                )
        );

        assertEquals(2, cartItem.getQuantity());

        verify(cartRepository, never())
                .save(any(Cart.class));
    }

    // =========================================================
    // removeProductFromCart()
    // =========================================================

    @Test
    void removeProductFromCart_shouldRemoveItem() {

        cart.getCartItems().add(cartItem);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.of(cartItem));

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result =
                cartService.removeProductFromCart(
                        1L,
                        100L
                );

        assertTrue(
                result.getCartItems().isEmpty()
        );

        verify(cartRepository).save(cart);
    }

    @Test
    void removeProductFromCart_shouldThrowWhenCartNotFound() {

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeProductFromCart(
                        1L,
                        100L
                )
        );
    }

    @Test
    void removeProductFromCart_shouldThrowWhenItemNotFound() {

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeProductFromCart(
                        1L,
                        100L
                )
        );
    }

    // =========================================================
    // checkout()
    // =========================================================

    @Test
    void checkout_shouldCreateOrderAndClearCart() {

        cart.getCartItems().add(cartItem);

        OrderResponse orderResponse =
                new OrderResponse(
                        1L,
                        "Test User",
                        "user@gmail.com",
                        LocalDateTime.now(),
                        OrderStatus.PLACED,
                        50000.0,
                        new ArrayList<>()
                );

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(orderService.saveOrder(
                any(),
                eq("user@gmail.com")
        )).thenReturn(orderResponse);

        when(cartRepository.save(cart))
                .thenReturn(cart);

        OrderResponse result =
                cartService.checkout(
                        1L,
                        "user@gmail.com",
                        1L
                );

        assertSame(
                orderResponse,
                result
        );

        assertTrue(
                cart.getCartItems().isEmpty()
        );

        verify(orderService).saveOrder(
                any(),
                eq("user@gmail.com")
        );

        verify(cartRepository).save(cart);
    }

    @Test
    void checkout_shouldThrowWhenCartNotFound() {

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.checkout(
                        1L,
                        "user@gmail.com",
                        1L
                )
        );

        verify(orderService, never())
                .saveOrder(any(), anyString());
    }

    @Test
    void checkout_shouldRejectEmptyCart() {

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.checkout(
                        1L,
                        "user@gmail.com",
                        1L
                )
        );

        verify(orderService, never())
                .saveOrder(any(), anyString());
    }

    @Test
    void checkout_shouldUseLoggedInEmail() {

        cart.getCartItems().add(cartItem);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(orderService.saveOrder(
                any(),
                eq("loggedin@gmail.com")
        )).thenReturn(new OrderResponse(
                1L,
                "Test User",
                "user@gmail.com",
                LocalDateTime.now(),
                OrderStatus.PLACED,
                50000.0,
                new ArrayList<>()
        ));

        when(cartRepository.save(cart))
                .thenReturn(cart);

        cartService.checkout(
                1L,
                "loggedin@gmail.com",
                1L
        );

        verify(orderService).saveOrder(
                any(),
                eq("loggedin@gmail.com")
        );
    }

    @Test
    void checkout_shouldNotClearCartWhenOrderCreationFails() {

        cart.getCartItems().add(cartItem);

        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(cart));

        when(orderService.saveOrder(
                any(),
                eq("user@gmail.com")
        )).thenThrow(
                new InsufficientStockException(
                        "Insufficient stock"
                )
        );

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.checkout(
                        1L,
                        "user@gmail.com",
                        1L
                )
        );

        assertEquals(
                1,
                cart.getCartItems().size()
        );

        verify(cartRepository, never())
                .save(cart);
    }
}
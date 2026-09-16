package com.example.ecommerce.controller;

import com.example.ecommerce.config.SecurityConfig;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.security.CustomUserDetailsService;
import com.example.ecommerce.security.JwtService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // 1. Anyone can view all products
    @Test
    void anyoneCanGetAllProducts() throws Exception {

        when(productService.getAllProducts())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/products")
                )
                .andExpect(status().isOk());

        verify(productService).getAllProducts();
    }


    // 2. Anyone can view a product by ID
    @Test
    void anyoneCanGetProductById() throws Exception {

        when(productService.getProductById(1L))
                .thenReturn(null);

        mockMvc.perform(
                        get("/api/products/1")
                )
                .andExpect(status().isOk());

        verify(productService).getProductById(1L);
    }


    // 3. Customer cannot create a product
    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCannotCreateProduct() throws Exception {

        mockMvc.perform(
                        post("/api/products")
                                .contentType("application/json")
                                .content("""
                                {
                                    "name": "Laptop",
                                    "price": 50000,
                                    "description": "Test laptop"
                                }
                                """)
                )
                .andExpect(status().isForbidden());

        verify(productService, never()).saveProduct(any());
    }


    // 4. Admin can create a product
    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanCreateProduct() throws Exception {

        Product product = new Product();

        when(productService.saveProduct(any(Product.class)))
                .thenReturn(product);

        mockMvc.perform(
                        post("/api/products")
                                .contentType("application/json")
                                .content("""
                                {
                                    "name": "Laptop",
                                    "price": 50000,
                                    "description": "Test laptop"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(productService).saveProduct(any(Product.class));
    }


    // 5. Customer cannot delete a product
    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCannotDeleteProduct() throws Exception {

        mockMvc.perform(
                        delete("/api/products/1")
                )
                .andExpect(status().isForbidden());

        verify(productService, never()).deleteProduct(1L);
    }


    // 6. Admin can delete a product
    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanDeleteProduct() throws Exception {

        doNothing().when(productService)
                .deleteProduct(1L);

        mockMvc.perform(
                        delete("/api/products/1")
                )
                .andExpect(status().isOk());

        verify(productService).deleteProduct(1L);
    }
}
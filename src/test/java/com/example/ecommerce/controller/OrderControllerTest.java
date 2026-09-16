package com.example.ecommerce.controller;

import org.springframework.context.annotation.Import;
import com.example.ecommerce.config.SecurityConfig;

import com.example.ecommerce.security.CustomUserDetailsService;
import com.example.ecommerce.security.JwtService;
import com.example.ecommerce.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // =====================================================
    // TEST 1
    // CUSTOMER CAN GET THEIR ORDERS
    // =====================================================

    @Test
    @WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void getMyOrders_shouldReturn200() throws Exception {

        when(orderService.getMyOrders("user@gmail.com"))
                .thenReturn(java.util.List.of());

        mockMvc.perform(
                        get("/api/orders/my-orders")
                )
                .andExpect(status().isOk());

        verify(orderService)
                .getMyOrders("user@gmail.com");
    }


    // =====================================================
    // TEST 2
    // CUSTOMER CAN GET THEIR ORDER BY ID
    // =====================================================

    @Test
    @WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void getMyOrderById_shouldReturn200() throws Exception {

        when(orderService.getMyOrderById(
                1L,
                "user@gmail.com"
        )).thenReturn(null);

        mockMvc.perform(
                        get("/api/orders/my-orders/1")
                )
                .andExpect(status().isOk());

        verify(orderService)
                .getMyOrderById(
                        1L,
                        "user@gmail.com"
                );
    }


    // =====================================================
    // TEST 3
    // CUSTOMER CANNOT GET ALL ORDERS
    // =====================================================

    @Test
    @WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCannotGetAllOrders() throws Exception {

        mockMvc.perform(
                        get("/api/orders")
                )
                .andExpect(status().isForbidden());

        verify(orderService, never())
                .getAllOrders();
    }


    // =====================================================
    // TEST 4
    // CUSTOMER CANNOT GET ADMIN ORDER BY ID
    // =====================================================

    @Test
    @WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCannotGetOrderById() throws Exception {

        mockMvc.perform(
                        get("/api/orders/1")
                )
                .andExpect(status().isForbidden());

        verify(orderService, never())
                .getOrderById(1L);
    }


    // =====================================================
    // TEST 5
    // ADMIN CAN GET ALL ORDERS
    // =====================================================

    @Test
    @WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanGetAllOrders() throws Exception {

        when(orderService.getAllOrders())
                .thenReturn(java.util.List.of());

        mockMvc.perform(
                        get("/api/orders")
                )
                .andExpect(status().isOk());

        verify(orderService)
                .getAllOrders();
    }


    // =====================================================
    // TEST 6
    // STAFF CAN GET ALL ORDERS
    // =====================================================

    @Test
    @WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCanGetAllOrders() throws Exception {

        when(orderService.getAllOrders())
                .thenReturn(java.util.List.of());

        mockMvc.perform(
                        get("/api/orders")
                )
                .andExpect(status().isOk());

        verify(orderService)
                .getAllOrders();
    }


    // =====================================================
    // TEST 7
    // CUSTOMER CAN CANCEL THEIR ORDER
    // =====================================================

    @Test
    @WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCanCancelOrder() throws Exception {

        when(orderService.cancelMyOrder(
                1L,
                "user@gmail.com"
        )).thenReturn(null);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders
                                .put("/api/orders/my-orders/1/cancel")
                )
                .andExpect(status().isOk());

        verify(orderService)
                .cancelMyOrder(
                        1L,
                        "user@gmail.com"
                );
    }
}
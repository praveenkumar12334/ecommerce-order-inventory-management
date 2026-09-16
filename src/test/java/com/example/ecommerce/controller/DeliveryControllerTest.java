package com.example.ecommerce.controller;

import com.example.ecommerce.config.SecurityConfig;
import com.example.ecommerce.dto.DeliveryRequest;
import com.example.ecommerce.dto.DeliveryResponse;
import com.example.ecommerce.entity.DeliveryStatus;
import com.example.ecommerce.security.CustomUserDetailsService;
import com.example.ecommerce.security.JwtService;
import com.example.ecommerce.service.DeliveryService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliveryController.class)
@Import(SecurityConfig.class)
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryService deliveryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // ---------------------------------------------------------
    // TEST 1: ADMIN can create delivery
    // ---------------------------------------------------------

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanCreateDelivery() throws Exception {

        when(deliveryService.createDelivery(
                anyLong(),
                any(DeliveryRequest.class)
        )).thenReturn(null);

        mockMvc.perform(
                        post("/api/deliveries/order/1")
                                .contentType("application/json")
                                .content("""
                                {
                                    "deliveryAddress": "Salem, Tamil Nadu",
                                    "deliveryPerson": "Ravi"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(deliveryService).createDelivery(
                eq(1L),
                any(DeliveryRequest.class)
        );
    }


    // ---------------------------------------------------------
    // TEST 2: STAFF can create delivery
    // ---------------------------------------------------------

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCanCreateDelivery() throws Exception {

        when(deliveryService.createDelivery(
                anyLong(),
                any(DeliveryRequest.class)
        )).thenReturn(null);

        mockMvc.perform(
                        post("/api/deliveries/order/2")
                                .contentType("application/json")
                                .content("""
                                {
                                    "deliveryAddress": "Chennai, Tamil Nadu",
                                    "deliveryPerson": "Kumar"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(deliveryService).createDelivery(
                eq(2L),
                any(DeliveryRequest.class)
        );
    }


    // ---------------------------------------------------------
    // TEST 3: ADMIN can assign delivery person
    // ---------------------------------------------------------

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanAssignDelivery() throws Exception {

        when(deliveryService.assignDelivery(
                anyLong(),
                eq("Ravi")
        )).thenReturn(null);

        mockMvc.perform(
                        put("/api/deliveries/1/assign")
                                .param("deliveryPerson", "Ravi")
                )
                .andExpect(status().isOk());

        verify(deliveryService).assignDelivery(
                eq(1L),
                eq("Ravi")
        );
    }


    // ---------------------------------------------------------
    // TEST 4: STAFF can assign delivery person
    // ---------------------------------------------------------

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCanAssignDelivery() throws Exception {

        when(deliveryService.assignDelivery(
                anyLong(),
                eq("Kumar")
        )).thenReturn(null);

        mockMvc.perform(
                        put("/api/deliveries/2/assign")
                                .param("deliveryPerson", "Kumar")
                )
                .andExpect(status().isOk());

        verify(deliveryService).assignDelivery(
                eq(2L),
                eq("Kumar")
        );
    }


    // ---------------------------------------------------------
    // TEST 5: ADMIN can update delivery status
    // ---------------------------------------------------------

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanUpdateDeliveryStatus() throws Exception {

        when(deliveryService.updateStatus(
                anyLong(),
                eq(DeliveryStatus.OUT_FOR_DELIVERY)
        )).thenReturn(null);

        mockMvc.perform(
                        put("/api/deliveries/1/status")
                                .param(
                                        "status",
                                        "OUT_FOR_DELIVERY"
                                )
                )
                .andExpect(status().isOk());

        verify(deliveryService).updateStatus(
                eq(1L),
                eq(DeliveryStatus.OUT_FOR_DELIVERY)
        );
    }


    // ---------------------------------------------------------
    // TEST 6: STAFF can update delivery status
    // ---------------------------------------------------------

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCanUpdateDeliveryStatus() throws Exception {

        when(deliveryService.updateStatus(
                anyLong(),
                eq(DeliveryStatus.DELIVERED)
        )).thenReturn(null);

        mockMvc.perform(
                        put("/api/deliveries/2/status")
                                .param(
                                        "status",
                                        "DELIVERED"
                                )
                )
                .andExpect(status().isOk());

        verify(deliveryService).updateStatus(
                eq(2L),
                eq(DeliveryStatus.DELIVERED)
        );
    }


    // ---------------------------------------------------------
    // TEST 7: ADMIN can get delivery by order
    // ---------------------------------------------------------

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanGetDelivery() throws Exception {

        when(deliveryService.getDeliveryByOrderId(
                anyLong()
        )).thenReturn(null);

        mockMvc.perform(
                        get("/api/deliveries/order/1")
                )
                .andExpect(status().isOk());

        verify(deliveryService).getDeliveryByOrderId(
                eq(1L)
        );
    }


    // ---------------------------------------------------------
    // TEST 8: STAFF can get delivery by order
    // ---------------------------------------------------------

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCanGetDelivery() throws Exception {

        when(deliveryService.getDeliveryByOrderId(
                anyLong()
        )).thenReturn(null);

        mockMvc.perform(
                        get("/api/deliveries/order/2")
                )
                .andExpect(status().isOk());

        verify(deliveryService).getDeliveryByOrderId(
                eq(2L)
        );
    }


    // ---------------------------------------------------------
    // TEST 9: CUSTOMER cannot create delivery
    // ---------------------------------------------------------

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCannotCreateDelivery() throws Exception {

        mockMvc.perform(
                        post("/api/deliveries/order/1")
                                .contentType("application/json")
                                .content("""
                                {
                                    "deliveryAddress": "Salem, Tamil Nadu",
                                    "deliveryPerson": "Ravi"
                                }
                                """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(deliveryService);
    }


    // ---------------------------------------------------------
    // TEST 10: CUSTOMER cannot assign delivery
    // ---------------------------------------------------------

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCannotAssignDelivery() throws Exception {

        mockMvc.perform(
                        put("/api/deliveries/1/assign")
                                .param("deliveryPerson", "Ravi")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(deliveryService);
    }


    // ---------------------------------------------------------
    // TEST 11: CUSTOMER cannot update delivery status
    // ---------------------------------------------------------

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCannotUpdateDeliveryStatus() throws Exception {

        mockMvc.perform(
                        put("/api/deliveries/1/status")
                                .param(
                                        "status",
                                        "DELIVERED"
                                )
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(deliveryService);
    }


    // ---------------------------------------------------------
    // TEST 12: CUSTOMER cannot get delivery
    // ---------------------------------------------------------

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCannotGetDelivery() throws Exception {

        mockMvc.perform(
                        get("/api/deliveries/order/1")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(deliveryService);
    }


    // ---------------------------------------------------------
    // TEST 13: Unauthenticated user cannot create delivery
    // ---------------------------------------------------------

    @Test
    void unauthenticatedUserCannotCreateDelivery()
            throws Exception {

        mockMvc.perform(
                        post("/api/deliveries/order/1")
                                .contentType("application/json")
                                .content("""
                                {
                                    "deliveryAddress": "Salem, Tamil Nadu",
                                    "deliveryPerson": "Ravi"
                                }
                                """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(deliveryService);
    }
}
package com.example.ecommerce.controller;

import com.example.ecommerce.config.SecurityConfig;
import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentResponse;
import com.example.ecommerce.entity.PaymentMethod;
import com.example.ecommerce.security.CustomUserDetailsService;
import com.example.ecommerce.security.JwtService;
import com.example.ecommerce.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // ---------------------------------------------------------
    // TEST 1: Customer can make UPI payment
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCanMakeUpiPayment() throws Exception {

        when(paymentService.makePayment(
                anyLong(),
                any(PaymentMethod.class),
                eq("user@gmail.com")
        )).thenReturn(null);

        mockMvc.perform(
                        post("/api/payments/orders/1")
                                .contentType("application/json")
                                .content("""
                                {
                                    "paymentMethod": "UPI"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(paymentService).makePayment(
                eq(1L),
                eq(PaymentMethod.UPI),
                eq("user@gmail.com")
        );
    }


    // ---------------------------------------------------------
    // TEST 2: Customer can make COD payment
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCanMakeCodPayment() throws Exception {

        when(paymentService.makePayment(
                anyLong(),
                any(PaymentMethod.class),
                eq("user@gmail.com")
        )).thenReturn(null);

        mockMvc.perform(
                        post("/api/payments/orders/2")
                                .contentType("application/json")
                                .content("""
                                {
                                    "paymentMethod": "COD"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(paymentService).makePayment(
                eq(2L),
                eq(PaymentMethod.COD),
                eq("user@gmail.com")
        );
    }


    // ---------------------------------------------------------
    // TEST 3: Customer can make credit card payment
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCanMakeCreditCardPayment() throws Exception {

        when(paymentService.makePayment(
                anyLong(),
                any(PaymentMethod.class),
                eq("user@gmail.com")
        )).thenReturn(null);

        mockMvc.perform(
                        post("/api/payments/orders/3")
                                .contentType("application/json")
                                .content("""
                                {
                                    "paymentMethod": "CREDIT_CARD"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(paymentService).makePayment(
                eq(3L),
                eq(PaymentMethod.CREDIT_CARD),
                eq("user@gmail.com")
        );
    }


    // ---------------------------------------------------------
    // TEST 4: Customer can make debit card payment
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCanMakeDebitCardPayment() throws Exception {

        when(paymentService.makePayment(
                anyLong(),
                any(PaymentMethod.class),
                eq("user@gmail.com")
        )).thenReturn(null);

        mockMvc.perform(
                        post("/api/payments/orders/4")
                                .contentType("application/json")
                                .content("""
                                {
                                    "paymentMethod": "DEBIT_CARD"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(paymentService).makePayment(
                eq(4L),
                eq(PaymentMethod.DEBIT_CARD),
                eq("user@gmail.com")
        );
    }


    // ---------------------------------------------------------
    // TEST 5: Unauthenticated user cannot make payment
    // ---------------------------------------------------------

    @Test
    void unauthenticatedUserCannotMakePayment() throws Exception {

        mockMvc.perform(
                        post("/api/payments/orders/5")
                                .contentType("application/json")
                                .content("""
                                {
                                    "paymentMethod": "UPI"
                                }
                                """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(paymentService);
    }


    // ---------------------------------------------------------
    // TEST 6: Admin can make payment
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanMakePayment() throws Exception {

        when(paymentService.makePayment(
                anyLong(),
                any(PaymentMethod.class),
                eq("admin@gmail.com")
        )).thenReturn(null);

        mockMvc.perform(
                        post("/api/payments/orders/6")
                                .contentType("application/json")
                                .content("""
                                {
                                    "paymentMethod": "UPI"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(paymentService).makePayment(
                eq(6L),
                eq(PaymentMethod.UPI),
                eq("admin@gmail.com")
        );
    }
}
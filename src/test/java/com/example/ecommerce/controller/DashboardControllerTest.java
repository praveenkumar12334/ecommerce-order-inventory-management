package com.example.ecommerce.controller;

import com.example.ecommerce.config.SecurityConfig;
import com.example.ecommerce.dto.DashboardResponse;
import com.example.ecommerce.security.CustomUserDetailsService;
import com.example.ecommerce.security.JwtService;
import com.example.ecommerce.service.DashboardService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // =========================================================
    // 1. ADMIN - GET DASHBOARD
    // =========================================================

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanGetDashboard() throws Exception {

        DashboardResponse response = new DashboardResponse();

        response.setTotalUsers(10);
        response.setTotalProducts(25);
        response.setTotalOrders(50);
        response.setTotalRevenue(125000.50);

        response.setPendingOrders(10);
        response.setConfirmedOrders(15);
        response.setShippedOrders(10);
        response.setDeliveredOrders(10);
        response.setCancelledOrders(5);

        when(dashboardService.getDashboard())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/admin/dashboard")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalProducts").value(25))
                .andExpect(jsonPath("$.totalOrders").value(50))
                .andExpect(jsonPath("$.totalRevenue").value(125000.50))
                .andExpect(jsonPath("$.pendingOrders").value(10))
                .andExpect(jsonPath("$.confirmedOrders").value(15))
                .andExpect(jsonPath("$.shippedOrders").value(10))
                .andExpect(jsonPath("$.deliveredOrders").value(10))
                .andExpect(jsonPath("$.cancelledOrders").value(5));

        verify(dashboardService).getDashboard();
    }


    // =========================================================
    // 2. STAFF - CANNOT GET DASHBOARD
    // =========================================================

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCannotGetDashboard() throws Exception {

        mockMvc.perform(
                        get("/api/admin/dashboard")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(dashboardService);
    }


    // =========================================================
    // 3. CUSTOMER - CANNOT GET DASHBOARD
    // =========================================================

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCannotGetDashboard() throws Exception {

        mockMvc.perform(
                        get("/api/admin/dashboard")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(dashboardService);
    }


    // =========================================================
    // 4. UNAUTHENTICATED - CANNOT GET DASHBOARD
    // =========================================================

    @Test
    void unauthenticatedUserCannotGetDashboard()
            throws Exception {

        mockMvc.perform(
                        get("/api/admin/dashboard")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(dashboardService);
    }


    // =========================================================
    // 5. ADMIN - EMPTY/ZERO DASHBOARD
    // =========================================================

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanGetEmptyDashboard() throws Exception {

        DashboardResponse response = new DashboardResponse();

        response.setTotalUsers(0);
        response.setTotalProducts(0);
        response.setTotalOrders(0);
        response.setTotalRevenue(0);

        response.setPendingOrders(0);
        response.setConfirmedOrders(0);
        response.setShippedOrders(0);
        response.setDeliveredOrders(0);
        response.setCancelledOrders(0);

        when(dashboardService.getDashboard())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/admin/dashboard")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(0))
                .andExpect(jsonPath("$.totalProducts").value(0))
                .andExpect(jsonPath("$.totalOrders").value(0))
                .andExpect(jsonPath("$.totalRevenue").value(0))
                .andExpect(jsonPath("$.pendingOrders").value(0))
                .andExpect(jsonPath("$.confirmedOrders").value(0))
                .andExpect(jsonPath("$.shippedOrders").value(0))
                .andExpect(jsonPath("$.deliveredOrders").value(0))
                .andExpect(jsonPath("$.cancelledOrders").value(0));

        verify(dashboardService).getDashboard();
    }


    // =========================================================
    // 6. ADMIN - VERIFY SERVICE CALLED ONLY ONCE
    // =========================================================

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminDashboardCallsServiceOnce() throws Exception {

        DashboardResponse response = new DashboardResponse();

        response.setTotalUsers(5);
        response.setTotalProducts(10);
        response.setTotalOrders(20);
        response.setTotalRevenue(50000);

        response.setPendingOrders(5);
        response.setConfirmedOrders(5);
        response.setShippedOrders(4);
        response.setDeliveredOrders(4);
        response.setCancelledOrders(2);

        when(dashboardService.getDashboard())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/admin/dashboard")
                )
                .andExpect(status().isOk());

        verify(dashboardService, times(1))
                .getDashboard();
    }
}
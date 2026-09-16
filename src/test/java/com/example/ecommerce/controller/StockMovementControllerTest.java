package com.example.ecommerce.controller;

import com.example.ecommerce.config.SecurityConfig;
import com.example.ecommerce.dto.StockMovementResponse;
import com.example.ecommerce.entity.StockMovementType;
import com.example.ecommerce.security.CustomUserDetailsService;
import com.example.ecommerce.security.JwtService;
import com.example.ecommerce.service.StockMovementService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockMovementController.class)
@Import(SecurityConfig.class)
class StockMovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockMovementService stockMovementService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // ---------------------------------------------------------
    // 1. ADMIN - GET ALL STOCK MOVEMENTS
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanGetAllMovements() throws Exception {

        StockMovementResponse movement =
                new StockMovementResponse(
                        1L,
                        1L,
                        "Laptop",
                        10,
                        StockMovementType.PURCHASE_RECEIVED,
                        100L,
                        "Stock received",
                        LocalDateTime.now()
                );

        when(stockMovementService.getAllMovements())
                .thenReturn(List.of(movement));

        mockMvc.perform(
                        get("/api/stock-movements")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].productName")
                        .value("Laptop"))
                .andExpect(jsonPath("$[0].quantity").value(10))
                .andExpect(jsonPath("$[0].type")
                        .value("PURCHASE_RECEIVED"))
                .andExpect(jsonPath("$[0].referenceId")
                        .value(100))
                .andExpect(jsonPath("$[0].note")
                        .value("Stock received"));
    }


    // ---------------------------------------------------------
    // 2. STAFF - GET ALL STOCK MOVEMENTS
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCanGetAllMovements() throws Exception {

        StockMovementResponse movement =
                new StockMovementResponse(
                        1L,
                        2L,
                        "Mouse",
                        5,
                        StockMovementType.MANUAL_ADJUSTMENT,
                        200L,
                        "Manual adjustment",
                        LocalDateTime.now()
                );

        when(stockMovementService.getAllMovements())
                .thenReturn(List.of(movement));

        mockMvc.perform(
                        get("/api/stock-movements")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productId").value(2))
                .andExpect(jsonPath("$[0].productName")
                        .value("Mouse"))
                .andExpect(jsonPath("$[0].quantity").value(5))
                .andExpect(jsonPath("$[0].type")
                        .value("MANUAL_ADJUSTMENT"));
    }


    // ---------------------------------------------------------
    // 3. CUSTOMER CANNOT GET ALL STOCK MOVEMENTS
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "customer@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCannotGetAllMovements() throws Exception {

        mockMvc.perform(
                        get("/api/stock-movements")
                )
                .andExpect(status().isForbidden());
    }


    // ---------------------------------------------------------
    // 4. UNAUTHENTICATED USER CANNOT GET ALL
    // ---------------------------------------------------------

    @Test
    void unauthenticatedUserCannotGetAllMovements()
            throws Exception {

        mockMvc.perform(
                        get("/api/stock-movements")
                )
                .andExpect(status().isForbidden());
    }


    // ---------------------------------------------------------
    // 5. ADMIN - GET PRODUCT HISTORY
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanGetProductHistory() throws Exception {

        StockMovementResponse movement =
                new StockMovementResponse(
                        1L,
                        1L,
                        "Laptop",
                        10,
                        StockMovementType.PURCHASE_RECEIVED,
                        100L,
                        "Stock received",
                        LocalDateTime.now()
                );

        when(stockMovementService.getProductHistory(1L))
                .thenReturn(List.of(movement));

        mockMvc.perform(
                        get("/api/stock-movements/product/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].productName")
                        .value("Laptop"))
                .andExpect(jsonPath("$[0].quantity").value(10))
                .andExpect(jsonPath("$[0].type")
                        .value("PURCHASE_RECEIVED"))
                .andExpect(jsonPath("$[0].referenceId")
                        .value(100))
                .andExpect(jsonPath("$[0].note")
                        .value("Stock received"));
    }


    // ---------------------------------------------------------
    // 6. STAFF - GET PRODUCT HISTORY
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCanGetProductHistory() throws Exception {

        StockMovementResponse movement =
                new StockMovementResponse(
                        2L,
                        2L,
                        "Mouse",
                        3,
                        StockMovementType.ORDER_PLACED,
                        50L,
                        "Stock reduced for order",
                        LocalDateTime.now()
                );

        when(stockMovementService.getProductHistory(2L))
                .thenReturn(List.of(movement));

        mockMvc.perform(
                        get("/api/stock-movements/product/2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].productId").value(2))
                .andExpect(jsonPath("$[0].productName")
                        .value("Mouse"))
                .andExpect(jsonPath("$[0].quantity").value(3))
                .andExpect(jsonPath("$[0].type")
                        .value("ORDER_PLACED"));
    }


    // ---------------------------------------------------------
    // 7. CUSTOMER CANNOT GET PRODUCT HISTORY
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "customer@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCannotGetProductHistory() throws Exception {

        mockMvc.perform(
                        get("/api/stock-movements/product/1")
                )
                .andExpect(status().isForbidden());
    }


    // ---------------------------------------------------------
    // 8. UNAUTHENTICATED USER CANNOT GET PRODUCT HISTORY
    // ---------------------------------------------------------

    @Test
    void unauthenticatedUserCannotGetProductHistory()
            throws Exception {

        mockMvc.perform(
                        get("/api/stock-movements/product/1")
                )
                .andExpect(status().isForbidden());
    }


    // ---------------------------------------------------------
    // 9. ADMIN - EMPTY STOCK MOVEMENT LIST
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminGetsEmptyMovementList() throws Exception {

        when(stockMovementService.getAllMovements())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/stock-movements")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    // ---------------------------------------------------------
    // 10. STAFF - EMPTY PRODUCT HISTORY
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffGetsEmptyProductHistory() throws Exception {

        when(stockMovementService.getProductHistory(999L))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/stock-movements/product/999")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
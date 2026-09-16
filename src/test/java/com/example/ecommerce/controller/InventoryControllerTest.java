package com.example.ecommerce.controller;

import com.example.ecommerce.config.SecurityConfig;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.security.CustomUserDetailsService;
import com.example.ecommerce.security.JwtService;
import com.example.ecommerce.service.InventoryService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(InventoryController.class)
@Import(SecurityConfig.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // 1. Admin can get all inventory
    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanGetAllInventory() throws Exception {

        when(inventoryService.getAllInventory())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/inventory")
                )
                .andExpect(status().isOk());

        verify(inventoryService).getAllInventory();
    }


    // 2. Staff can get all inventory
    @Test
    @WithMockUser(username = "staff@gmail.com", roles = "STAFF")
    void staffCanGetAllInventory() throws Exception {

        when(inventoryService.getAllInventory())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/inventory")
                )
                .andExpect(status().isOk());

        verify(inventoryService).getAllInventory();
    }


    // 3. Customer cannot get all inventory
    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCannotGetAllInventory() throws Exception {

        mockMvc.perform(
                        get("/api/inventory")
                )
                .andExpect(status().isForbidden());

        verify(inventoryService, never()).getAllInventory();
    }


    // 4. Admin can get inventory for a product
    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanGetInventoryByProductId() throws Exception {

        when(inventoryService.getInventoryByProductId(1L))
                .thenReturn(null);

        mockMvc.perform(
                        get("/api/inventory/product/1")
                )
                .andExpect(status().isOk());

        verify(inventoryService)
                .getInventoryByProductId(1L);
    }


    // 5. Customer cannot get low-stock products
    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCannotGetLowStockProducts() throws Exception {

        mockMvc.perform(
                        get("/api/inventory/low-stock")
                )
                .andExpect(status().isForbidden());

        verify(inventoryService, never()).getLowStockProducts();
    }


    // 6. Staff can get low-stock products
    @Test
    @WithMockUser(username = "staff@gmail.com", roles = "STAFF")
    void staffCanGetLowStockProducts() throws Exception {

        when(inventoryService.getLowStockProducts())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/inventory/low-stock")
                )
                .andExpect(status().isOk());

        verify(inventoryService).getLowStockProducts();
    }


    // 7. Customer cannot update stock
    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCannotUpdateStock() throws Exception {

        mockMvc.perform(
                        put("/api/inventory/1")
                                .param("quantity", "10")
                )
                .andExpect(status().isForbidden());

        verify(inventoryService, never())
                .updateStock(anyLong(), anyInt());
    }


    // 8. Admin can update stock
    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanUpdateStock() throws Exception {

        when(inventoryService.updateStock(1L, 10))
                .thenReturn(null);

        mockMvc.perform(
                        put("/api/inventory/1")
                                .param("quantity", "10")
                )
                .andExpect(status().isOk());

        verify(inventoryService)
                .updateStock(1L, 10);
    }


    // 9. Customer cannot manually adjust stock
    @Test
    @WithMockUser(username = "user@gmail.com", roles = "CUSTOMER")
    void customerCannotAdjustStock() throws Exception {

        mockMvc.perform(
                        put("/api/inventory/1/adjust")
                                .param("quantity", "5")
                                .param("reason", "Damaged stock")
                )
                .andExpect(status().isForbidden());

        verify(inventoryService, never())
                .adjustStock(anyLong(), anyInt(), any());
    }


    // 10. Admin can manually adjust stock
    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanAdjustStock() throws Exception {

        when(inventoryService.adjustStock(
                eq(1L),
                eq(5),
                eq("Damaged stock")
        )).thenReturn(null);

        mockMvc.perform(
                        put("/api/inventory/1/adjust")
                                .param("quantity", "5")
                                .param("reason", "Damaged stock")
                )
                .andExpect(status().isOk());

        verify(inventoryService)
                .adjustStock(1L, 5, "Damaged stock");
    }
}
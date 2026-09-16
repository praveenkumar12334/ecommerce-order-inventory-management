package com.example.ecommerce.controller;

import com.example.ecommerce.config.SecurityConfig;
import com.example.ecommerce.dto.PurchaseOrderRequest;
import com.example.ecommerce.entity.PurchaseOrder;
import com.example.ecommerce.entity.PurchaseOrderStatus;
import com.example.ecommerce.service.PurchaseOrderService;
import com.example.ecommerce.security.CustomUserDetailsService;
import com.example.ecommerce.security.JwtService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseOrderController.class)
@Import(SecurityConfig.class)
class PurchaseOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PurchaseOrderService purchaseOrderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // ---------------------------------------------------------
    // 1. ADMIN - CREATE PURCHASE ORDER
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanCreatePurchaseOrder() throws Exception {

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setId(1L);
        purchaseOrder.setStatus(PurchaseOrderStatus.CREATED);

        when(purchaseOrderService.createPurchaseOrder(
                any(PurchaseOrderRequest.class)
        )).thenReturn(purchaseOrder);

        String requestJson = """
                {
                    "supplierId": 1,
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 10,
                            "purchasePrice": 500
                        }
                    ]
                }
                """;

        mockMvc.perform(
                        post("/api/purchase-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status")
                        .value("CREATED"));
    }


    // ---------------------------------------------------------
    // 2. STAFF CANNOT CREATE PURCHASE ORDER
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "staff@gmail.com", roles = "STAFF")
    void staffCannotCreatePurchaseOrder() throws Exception {

        String requestJson = """
                {
                    "supplierId": 1,
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 10,
                            "purchasePrice": 500
                        }
                    ]
                }
                """;

        mockMvc.perform(
                        post("/api/purchase-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isForbidden());
    }


    // ---------------------------------------------------------
    // 3. CUSTOMER CANNOT CREATE PURCHASE ORDER
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "customer@gmail.com", roles = "CUSTOMER")
    void customerCannotCreatePurchaseOrder() throws Exception {

        String requestJson = """
                {
                    "supplierId": 1,
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 10,
                            "purchasePrice": 500
                        }
                    ]
                }
                """;

        mockMvc.perform(
                        post("/api/purchase-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isForbidden());
    }


    // ---------------------------------------------------------
    // 4. ADMIN - GET ALL PURCHASE ORDERS
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanGetAllPurchaseOrders() throws Exception {

        PurchaseOrder order1 = new PurchaseOrder();
        order1.setId(1L);
        order1.setStatus(PurchaseOrderStatus.CREATED);

        PurchaseOrder order2 = new PurchaseOrder();
        order2.setId(2L);
        order2.setStatus(PurchaseOrderStatus.ORDERED);

        when(purchaseOrderService.getAllPurchaseOrders())
                .thenReturn(List.of(order1, order2));

        mockMvc.perform(
                        get("/api/purchase-orders")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }


    // ---------------------------------------------------------
    // 5. STAFF - GET ALL PURCHASE ORDERS
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "staff@gmail.com", roles = "STAFF")
    void staffCanGetAllPurchaseOrders() throws Exception {

        PurchaseOrder order = new PurchaseOrder();
        order.setId(1L);
        order.setStatus(PurchaseOrderStatus.CREATED);

        when(purchaseOrderService.getAllPurchaseOrders())
                .thenReturn(List.of(order));

        mockMvc.perform(
                        get("/api/purchase-orders")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }


    // ---------------------------------------------------------
    // 6. CUSTOMER CANNOT GET ALL PURCHASE ORDERS
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "customer@gmail.com", roles = "CUSTOMER")
    void customerCannotGetAllPurchaseOrders() throws Exception {

        mockMvc.perform(
                        get("/api/purchase-orders")
                )
                .andExpect(status().isForbidden());
    }


    // ---------------------------------------------------------
    // 7. ADMIN - GET PURCHASE ORDER BY ID
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanGetPurchaseOrderById() throws Exception {

        PurchaseOrder order = new PurchaseOrder();
        order.setId(1L);
        order.setStatus(PurchaseOrderStatus.CREATED);

        when(purchaseOrderService.getPurchaseOrderById(1L))
                .thenReturn(order);

        mockMvc.perform(
                        get("/api/purchase-orders/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status")
                        .value("CREATED"));
    }


    // ---------------------------------------------------------
    // 8. STAFF - GET PURCHASE ORDER BY ID
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "staff@gmail.com", roles = "STAFF")
    void staffCanGetPurchaseOrderById() throws Exception {

        PurchaseOrder order = new PurchaseOrder();
        order.setId(1L);
        order.setStatus(PurchaseOrderStatus.ORDERED);

        when(purchaseOrderService.getPurchaseOrderById(1L))
                .thenReturn(order);

        mockMvc.perform(
                        get("/api/purchase-orders/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status")
                        .value("ORDERED"));
    }


    // ---------------------------------------------------------
    // 9. CUSTOMER CANNOT GET PURCHASE ORDER BY ID
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "customer@gmail.com", roles = "CUSTOMER")
    void customerCannotGetPurchaseOrderById() throws Exception {

        mockMvc.perform(
                        get("/api/purchase-orders/1")
                )
                .andExpect(status().isForbidden());
    }


    // ---------------------------------------------------------
    // 10. ADMIN - MARK AS ORDERED
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanMarkPurchaseOrderAsOrdered() throws Exception {

        PurchaseOrder order = new PurchaseOrder();
        order.setId(1L);
        order.setStatus(PurchaseOrderStatus.ORDERED);

        when(purchaseOrderService.markAsOrdered(1L))
                .thenReturn(order);

        mockMvc.perform(
                        put("/api/purchase-orders/1/ordered")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status")
                        .value("ORDERED"));
    }


    // ---------------------------------------------------------
    // 11. STAFF CANNOT MARK AS ORDERED
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "staff@gmail.com", roles = "STAFF")
    void staffCannotMarkPurchaseOrderAsOrdered()
            throws Exception {

        mockMvc.perform(
                        put("/api/purchase-orders/1/ordered")
                )
                .andExpect(status().isForbidden());
    }


    // ---------------------------------------------------------
    // 12. CUSTOMER CANNOT MARK AS ORDERED
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "customer@gmail.com", roles = "CUSTOMER")
    void customerCannotMarkPurchaseOrderAsOrdered()
            throws Exception {

        mockMvc.perform(
                        put("/api/purchase-orders/1/ordered")
                )
                .andExpect(status().isForbidden());
    }


    // ---------------------------------------------------------
    // 13. ADMIN - RECEIVE PURCHASE ORDER
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void adminCanReceivePurchaseOrder() throws Exception {

        PurchaseOrder order = new PurchaseOrder();
        order.setId(1L);
        order.setStatus(PurchaseOrderStatus.RECEIVED);

        when(purchaseOrderService.receivePurchaseOrder(1L))
                .thenReturn(order);

        mockMvc.perform(
                        put("/api/purchase-orders/1/receive")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status")
                        .value("RECEIVED"));
    }


    // ---------------------------------------------------------
    // 14. STAFF - RECEIVE PURCHASE ORDER
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "staff@gmail.com", roles = "STAFF")
    void staffCanReceivePurchaseOrder() throws Exception {

        PurchaseOrder order = new PurchaseOrder();
        order.setId(1L);
        order.setStatus(PurchaseOrderStatus.RECEIVED);

        when(purchaseOrderService.receivePurchaseOrder(1L))
                .thenReturn(order);

        mockMvc.perform(
                        put("/api/purchase-orders/1/receive")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status")
                        .value("RECEIVED"));
    }


    // ---------------------------------------------------------
    // 15. CUSTOMER CANNOT RECEIVE PURCHASE ORDER
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "customer@gmail.com", roles = "CUSTOMER")
    void customerCannotReceivePurchaseOrder()
            throws Exception {

        mockMvc.perform(
                        put("/api/purchase-orders/1/receive")
                )
                .andExpect(status().isForbidden());
    }


    // ---------------------------------------------------------
    // 16. UNAUTHENTICATED USER CANNOT CREATE
    // ---------------------------------------------------------

    @Test
    void unauthenticatedUserCannotCreatePurchaseOrder()
            throws Exception {

        String requestJson = """
                {
                    "supplierId": 1,
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 10,
                            "purchasePrice": 500
                        }
                    ]
                }
                """;

        mockMvc.perform(
                        post("/api/purchase-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isForbidden());
    }


    // ---------------------------------------------------------
    // 17. UNAUTHENTICATED USER CANNOT GET ALL
    // ---------------------------------------------------------

    @Test
    void unauthenticatedUserCannotGetAllPurchaseOrders()
            throws Exception {

        mockMvc.perform(
                        get("/api/purchase-orders")
                )
                .andExpect(status().isForbidden());
    }
}
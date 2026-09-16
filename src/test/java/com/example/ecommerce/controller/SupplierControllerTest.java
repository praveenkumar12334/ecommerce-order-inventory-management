package com.example.ecommerce.controller;

import com.example.ecommerce.config.SecurityConfig;
import com.example.ecommerce.entity.Supplier;
import com.example.ecommerce.security.CustomUserDetailsService;
import com.example.ecommerce.security.JwtService;
import com.example.ecommerce.service.SupplierService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupplierController.class)
@Import(SecurityConfig.class)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupplierService supplierService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // ---------------------------------------------------------
    // TEST 1: ADMIN can create supplier
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanCreateSupplier() throws Exception {

        when(supplierService.createSupplier(
                any(Supplier.class)
        )).thenReturn(null);

        mockMvc.perform(
                        post("/api/suppliers")
                                .contentType("application/json")
                                .content("""
                                {
                                    "name": "ABC Suppliers",
                                    "email": "abc@gmail.com",
                                    "phone": "9876543210",
                                    "address": "Salem, Tamil Nadu"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(supplierService).createSupplier(
                any(Supplier.class)
        );
    }


    // ---------------------------------------------------------
    // TEST 2: ADMIN can get all suppliers
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanGetAllSuppliers() throws Exception {

        when(supplierService.getAllSuppliers())
                .thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(
                        get("/api/suppliers")
                )
                .andExpect(status().isOk());

        verify(supplierService).getAllSuppliers();
    }


    // ---------------------------------------------------------
    // TEST 3: STAFF can get all suppliers
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCanGetAllSuppliers() throws Exception {

        when(supplierService.getAllSuppliers())
                .thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(
                        get("/api/suppliers")
                )
                .andExpect(status().isOk());

        verify(supplierService).getAllSuppliers();
    }


    // ---------------------------------------------------------
    // TEST 4: ADMIN can get supplier by ID
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanGetSupplierById() throws Exception {

        when(supplierService.getSupplierById(
                eq(1L)
        )).thenReturn(null);

        mockMvc.perform(
                        get("/api/suppliers/1")
                )
                .andExpect(status().isOk());

        verify(supplierService).getSupplierById(
                eq(1L)
        );
    }


    // ---------------------------------------------------------
    // TEST 5: STAFF can get supplier by ID
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCanGetSupplierById() throws Exception {

        when(supplierService.getSupplierById(
                eq(1L)
        )).thenReturn(null);

        mockMvc.perform(
                        get("/api/suppliers/1")
                )
                .andExpect(status().isOk());

        verify(supplierService).getSupplierById(
                eq(1L)
        );
    }


    // ---------------------------------------------------------
    // TEST 6: ADMIN can update supplier
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanUpdateSupplier() throws Exception {

        when(supplierService.updateSupplier(
                eq(1L),
                any(Supplier.class)
        )).thenReturn(null);

        mockMvc.perform(
                        put("/api/suppliers/1")
                                .contentType("application/json")
                                .content("""
                                {
                                    "name": "Updated Suppliers",
                                    "email": "updated@gmail.com",
                                    "phone": "9876543210",
                                    "address": "Chennai, Tamil Nadu"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(supplierService).updateSupplier(
                eq(1L),
                any(Supplier.class)
        );
    }


    // ---------------------------------------------------------
    // TEST 7: ADMIN can delete supplier
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanDeleteSupplier() throws Exception {

        doNothing().when(supplierService)
                .deleteSupplier(eq(1L));

        mockMvc.perform(
                        delete("/api/suppliers/1")
                )
                .andExpect(status().isOk());

        verify(supplierService).deleteSupplier(
                eq(1L)
        );
    }


    // ---------------------------------------------------------
    // TEST 8: CUSTOMER cannot create supplier
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCannotCreateSupplier() throws Exception {

        mockMvc.perform(
                        post("/api/suppliers")
                                .contentType("application/json")
                                .content("""
                                {
                                    "name": "ABC Suppliers",
                                    "email": "abc@gmail.com",
                                    "phone": "9876543210",
                                    "address": "Salem, Tamil Nadu"
                                }
                                """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(supplierService);
    }


    // ---------------------------------------------------------
    // TEST 9: CUSTOMER cannot get suppliers
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCannotGetAllSuppliers() throws Exception {

        mockMvc.perform(
                        get("/api/suppliers")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(supplierService);
    }


    // ---------------------------------------------------------
    // TEST 10: STAFF cannot create supplier
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCannotCreateSupplier() throws Exception {

        mockMvc.perform(
                        post("/api/suppliers")
                                .contentType("application/json")
                                .content("""
                                {
                                    "name": "ABC Suppliers",
                                    "email": "abc@gmail.com",
                                    "phone": "9876543210",
                                    "address": "Salem, Tamil Nadu"
                                }
                                """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(supplierService);
    }


    // ---------------------------------------------------------
    // TEST 11: STAFF cannot update supplier
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCannotUpdateSupplier() throws Exception {

        mockMvc.perform(
                        put("/api/suppliers/1")
                                .contentType("application/json")
                                .content("""
                                {
                                    "name": "Updated Suppliers",
                                    "email": "updated@gmail.com",
                                    "phone": "9876543210",
                                    "address": "Chennai, Tamil Nadu"
                                }
                                """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(supplierService);
    }


    // ---------------------------------------------------------
    // TEST 12: STAFF cannot delete supplier
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "staff@gmail.com",
            roles = "STAFF"
    )
    void staffCannotDeleteSupplier() throws Exception {

        mockMvc.perform(
                        delete("/api/suppliers/1")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(supplierService);
    }


    // ---------------------------------------------------------
    // TEST 13: Unauthenticated user cannot create supplier
    // ---------------------------------------------------------

    @Test
    void unauthenticatedUserCannotCreateSupplier()
            throws Exception {

        mockMvc.perform(
                        post("/api/suppliers")
                                .contentType("application/json")
                                .content("""
                                {
                                    "name": "ABC Suppliers",
                                    "email": "abc@gmail.com",
                                    "phone": "9876543210",
                                    "address": "Salem, Tamil Nadu"
                                }
                                """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(supplierService);
    }


    // ---------------------------------------------------------
    // TEST 14: Unauthenticated user cannot get suppliers
    // ---------------------------------------------------------

    @Test
    void unauthenticatedUserCannotGetSuppliers()
            throws Exception {

        mockMvc.perform(
                        get("/api/suppliers")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(supplierService);
    }
}
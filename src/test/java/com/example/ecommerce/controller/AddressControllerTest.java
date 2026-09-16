package com.example.ecommerce.controller;

import com.example.ecommerce.config.SecurityConfig;
import com.example.ecommerce.dto.AddressRequest;
import com.example.ecommerce.security.CustomUserDetailsService;
import com.example.ecommerce.security.JwtService;
import com.example.ecommerce.service.AddressService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
@Import(SecurityConfig.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // ---------------------------------------------------------
    // TEST 1: CUSTOMER can add address
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCanAddAddress() throws Exception {

        when(addressService.addAddress(
                any(AddressRequest.class),
                eq("user@gmail.com")
        )).thenReturn(null);

        mockMvc.perform(
                        post("/api/addresses")
                                .contentType("application/json")
                                .content("""
                                {
                                    "fullName": "Praveen Kumar",
                                    "phone": "9876543210",
                                    "addressLine": "Main Street",
                                    "city": "Salem",
                                    "state": "Tamil Nadu",
                                    "pincode": "636001"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(addressService).addAddress(
                any(AddressRequest.class),
                eq("user@gmail.com")
        );
    }


    // ---------------------------------------------------------
    // TEST 2: CUSTOMER can get all own addresses
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCanGetMyAddresses() throws Exception {

        when(addressService.getMyAddresses(
                eq("user@gmail.com")
        )).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(
                        get("/api/addresses")
                )
                .andExpect(status().isOk());

        verify(addressService).getMyAddresses(
                eq("user@gmail.com")
        );
    }


    // ---------------------------------------------------------
    // TEST 3: CUSTOMER can get own address by ID
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCanGetMyAddress() throws Exception {

        when(addressService.getMyAddress(
                eq(1L),
                eq("user@gmail.com")
        )).thenReturn(null);

        mockMvc.perform(
                        get("/api/addresses/1")
                )
                .andExpect(status().isOk());

        verify(addressService).getMyAddress(
                eq(1L),
                eq("user@gmail.com")
        );
    }


    // ---------------------------------------------------------
    // TEST 4: CUSTOMER can update own address
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCanUpdateAddress() throws Exception {

        when(addressService.updateAddress(
                eq(1L),
                any(AddressRequest.class),
                eq("user@gmail.com")
        )).thenReturn(null);

        mockMvc.perform(
                        put("/api/addresses/1")
                                .contentType("application/json")
                                .content("""
                                {
                                    "fullName": "Praveen Kumar",
                                    "phone": "9876543210",
                                    "addressLine": "Updated Street",
                                    "city": "Salem",
                                    "state": "Tamil Nadu",
                                    "pincode": "636002"
                                }
                                """)
                )
                .andExpect(status().isOk());

        verify(addressService).updateAddress(
                eq(1L),
                any(AddressRequest.class),
                eq("user@gmail.com")
        );
    }


    // ---------------------------------------------------------
    // TEST 5: CUSTOMER can delete own address
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "user@gmail.com",
            roles = "CUSTOMER"
    )
    void customerCanDeleteAddress() throws Exception {

        doNothing().when(addressService).deleteAddress(
                eq(1L),
                eq("user@gmail.com")
        );

        mockMvc.perform(
                        delete("/api/addresses/1")
                )
                .andExpect(status().isOk());

        verify(addressService).deleteAddress(
                eq(1L),
                eq("user@gmail.com")
        );
    }


    // ---------------------------------------------------------
    // TEST 6: ADMIN can use address endpoints
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "admin@gmail.com",
            roles = "ADMIN"
    )
    void adminCanGetAddresses() throws Exception {

        when(addressService.getMyAddresses(
                eq("admin@gmail.com")
        )).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(
                        get("/api/addresses")
                )
                .andExpect(status().isOk());

        verify(addressService).getMyAddresses(
                eq("admin@gmail.com")
        );
    }


    // ---------------------------------------------------------
    // TEST 7: Unauthenticated user cannot add address
    // ---------------------------------------------------------

    @Test
    void unauthenticatedUserCannotAddAddress() throws Exception {

        mockMvc.perform(
                        post("/api/addresses")
                                .contentType("application/json")
                                .content("""
                                {
                                    "fullName": "Test User",
                                    "phone": "9876543210",
                                    "addressLine": "Main Street",
                                    "city": "Salem",
                                    "state": "Tamil Nadu",
                                    "pincode": "636001"
                                }
                                """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(addressService);
    }


    // ---------------------------------------------------------
    // TEST 8: Unauthenticated user cannot get addresses
    // ---------------------------------------------------------

    @Test
    void unauthenticatedUserCannotGetAddresses() throws Exception {

        mockMvc.perform(
                        get("/api/addresses")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(addressService);
    }


    // ---------------------------------------------------------
    // TEST 9: CUSTOMER can only call service with own email
    // ---------------------------------------------------------

    @Test
    @WithMockUser(
            username = "customer@gmail.com",
            roles = "CUSTOMER"
    )
    void customerAddressRequestUsesLoggedInEmail() throws Exception {

        when(addressService.getMyAddress(
                eq(10L),
                eq("customer@gmail.com")
        )).thenReturn(null);

        mockMvc.perform(
                        get("/api/addresses/10")
                )
                .andExpect(status().isOk());

        verify(addressService).getMyAddress(
                eq(10L),
                eq("customer@gmail.com")
        );

        verify(addressService, never()).getMyAddress(
                eq(10L),
                eq("another@gmail.com")
        );
    }
}
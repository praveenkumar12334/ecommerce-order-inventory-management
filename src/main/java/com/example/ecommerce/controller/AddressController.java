package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AddressRequest;
import com.example.ecommerce.entity.Address;
import com.example.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public Address addAddress(
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return addressService.addAddress(
                request,
                userDetails.getUsername());
    }

    @GetMapping
    public List<Address> getMyAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {

        return addressService.getMyAddresses(
                userDetails.getUsername());
    }

    @GetMapping("/{addressId}")
    public Address getMyAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return addressService.getMyAddress(
                addressId,
                userDetails.getUsername());
    }

    @PutMapping("/{addressId}")
    public Address updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return addressService.updateAddress(
                addressId,
                request,
                userDetails.getUsername());
    }

    @DeleteMapping("/{addressId}")
    public String deleteAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal UserDetails userDetails) {

        addressService.deleteAddress(
                addressId,
                userDetails.getUsername());

        return "Address deleted successfully";
    }
}
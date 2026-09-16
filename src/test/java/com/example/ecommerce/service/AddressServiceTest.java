package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddressRequest;
import com.example.ecommerce.entity.Address;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.AccessDeniedException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.AddressRepository;
import com.example.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User user;
    private Address address;
    private AddressRequest request;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("user@gmail.com");

        address = new Address();
        address.setFullName("Test User");
        address.setPhone("9876543210");
        address.setAddressLine("123 Main Street");
        address.setCity("Chennai");
        address.setState("Tamil Nadu");
        address.setPincode("600001");
        address.setUser(user);

        request = new AddressRequest();
        request.setFullName("Test User");
        request.setPhone("9876543210");
        request.setAddressLine("123 Main Street");
        request.setCity("Chennai");
        request.setState("Tamil Nadu");
        request.setPincode("600001");
    }

    // =========================================================
    // addAddress()
    // =========================================================

    @Test
    void addAddress_shouldAddSuccessfully() {

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(addressRepository.save(any(Address.class)))
                .thenReturn(address);

        Address result =
                addressService.addAddress(
                        request,
                        "user@gmail.com"
                );

        assertNotNull(result);
        assertEquals("Test User", result.getFullName());
        assertEquals("9876543210", result.getPhone());
        assertEquals("123 Main Street", result.getAddressLine());
        assertEquals("Chennai", result.getCity());
        assertEquals("Tamil Nadu", result.getState());
        assertEquals("600001", result.getPincode());
        assertEquals(user, result.getUser());

        verify(userRepository)
                .findByEmail("user@gmail.com");

        verify(addressRepository)
                .save(any(Address.class));
    }

    @Test
    void addAddress_shouldThrowWhenUserNotFound() {

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.addAddress(
                        request,
                        "unknown@gmail.com"
                )
        );

        verify(userRepository)
                .findByEmail("unknown@gmail.com");

        verify(addressRepository, never())
                .save(any(Address.class));
    }

    // =========================================================
    // getMyAddresses()
    // =========================================================

    @Test
    void getMyAddresses_shouldReturnUserAddresses() {

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByUserId(1L))
                .thenReturn(List.of(address));

        List<Address> result =
                addressService.getMyAddresses(
                        "user@gmail.com"
                );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(
                "Test User",
                result.getFirst().getFullName()
        );
        assertEquals(
                "Chennai",
                result.getFirst().getCity()
        );

        verify(userRepository)
                .findByEmail("user@gmail.com");

        verify(addressRepository)
                .findByUserId(1L);
    }

    @Test
    void getMyAddresses_shouldReturnEmptyListWhenNoAddresses() {

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByUserId(1L))
                .thenReturn(List.of());

        List<Address> result =
                addressService.getMyAddresses(
                        "user@gmail.com"
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(addressRepository)
                .findByUserId(1L);
    }

    @Test
    void getMyAddresses_shouldThrowWhenUserNotFound() {

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.getMyAddresses(
                        "unknown@gmail.com"
                )
        );

        verify(addressRepository, never())
                .findByUserId(anyLong());
    }

    // =========================================================
    // getMyAddress()
    // =========================================================

    @Test
    void getMyAddress_shouldReturnOwnAddress() {

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(address));

        Address result =
                addressService.getMyAddress(
                        1L,
                        "user@gmail.com"
                );

        assertNotNull(result);
        assertEquals("Test User", result.getFullName());
        assertEquals(user, result.getUser());

        verify(userRepository)
                .findByEmail("user@gmail.com");

        verify(addressRepository)
                .findByIdAndUserId(1L, 1L);
    }

    @Test
    void getMyAddress_shouldThrowAccessDeniedWhenAddressDoesNotBelongToUser() {

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(99L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> addressService.getMyAddress(
                        99L,
                        "user@gmail.com"
                )
        );

        verify(addressRepository)
                .findByIdAndUserId(99L, 1L);
    }

    @Test
    void getMyAddress_shouldThrowWhenUserNotFound() {

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.getMyAddress(
                        1L,
                        "unknown@gmail.com"
                )
        );

        verify(addressRepository, never())
                .findByIdAndUserId(
                        anyLong(),
                        anyLong()
                );
    }

    // =========================================================
    // updateAddress()
    // =========================================================

    @Test
    void updateAddress_shouldUpdateOwnAddress() {

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(address));

        when(addressRepository.save(address))
                .thenReturn(address);

        AddressRequest updatedRequest =
                new AddressRequest();

        updatedRequest.setFullName("Updated User");
        updatedRequest.setPhone("9999999999");
        updatedRequest.setAddressLine("456 New Street");
        updatedRequest.setCity("Coimbatore");
        updatedRequest.setState("Tamil Nadu");
        updatedRequest.setPincode("641001");

        Address result =
                addressService.updateAddress(
                        1L,
                        updatedRequest,
                        "user@gmail.com"
                );

        assertNotNull(result);

        assertEquals(
                "Updated User",
                result.getFullName()
        );

        assertEquals(
                "9999999999",
                result.getPhone()
        );

        assertEquals(
                "456 New Street",
                result.getAddressLine()
        );

        assertEquals(
                "Coimbatore",
                result.getCity()
        );

        assertEquals(
                "Tamil Nadu",
                result.getState()
        );

        assertEquals(
                "641001",
                result.getPincode()
        );

        verify(addressRepository)
                .save(address);
    }

    @Test
    void updateAddress_shouldThrowAccessDeniedForAnotherUsersAddress() {

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(99L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> addressService.updateAddress(
                        99L,
                        request,
                        "user@gmail.com"
                )
        );

        verify(addressRepository, never())
                .save(any(Address.class));
    }

    // =========================================================
    // deleteAddress()
    // =========================================================

    @Test
    void deleteAddress_shouldDeleteOwnAddress() {

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(address));

        doNothing()
                .when(addressRepository)
                .delete(address);

        addressService.deleteAddress(
                1L,
                "user@gmail.com"
        );

        verify(addressRepository)
                .findByIdAndUserId(1L, 1L);

        verify(addressRepository)
                .delete(address);
    }

    @Test
    void deleteAddress_shouldThrowAccessDeniedForAnotherUsersAddress() {

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(99L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> addressService.deleteAddress(
                        99L,
                        "user@gmail.com"
                )
        );

        verify(addressRepository, never())
                .delete(any(Address.class));
    }
}
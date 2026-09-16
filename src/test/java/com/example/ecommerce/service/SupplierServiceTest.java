package com.example.ecommerce.service;

import com.example.ecommerce.entity.Supplier;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier supplier;

    @BeforeEach
    void setUp() {

        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("ABC Electronics");
        supplier.setEmail("abc@gmail.com");
        supplier.setPhone("9876543210");
        supplier.setAddress("Chennai, Tamil Nadu");
    }

    // =========================================================
    // createSupplier()
    // =========================================================

    @Test
    void createSupplier_shouldCreateSuccessfully() {

        when(supplierRepository.save(supplier))
                .thenReturn(supplier);

        Supplier result =
                supplierService.createSupplier(supplier);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(
                "ABC Electronics",
                result.getName()
        );
        assertEquals(
                "abc@gmail.com",
                result.getEmail()
        );
        assertEquals(
                "9876543210",
                result.getPhone()
        );
        assertEquals(
                "Chennai, Tamil Nadu",
                result.getAddress()
        );

        verify(supplierRepository)
                .save(supplier);
    }

    // =========================================================
    // getAllSuppliers()
    // =========================================================

    @Test
    void getAllSuppliers_shouldReturnAllSuppliers() {

        Supplier supplier2 = new Supplier();
        supplier2.setId(2L);
        supplier2.setName("XYZ Technologies");
        supplier2.setEmail("xyz@gmail.com");
        supplier2.setPhone("9999999999");
        supplier2.setAddress("Coimbatore, Tamil Nadu");

        when(supplierRepository.findAll())
                .thenReturn(List.of(
                        supplier,
                        supplier2
                ));

        List<Supplier> result =
                supplierService.getAllSuppliers();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(
                "ABC Electronics",
                result.getFirst().getName()
        );

        assertEquals(
                "XYZ Technologies",
                result.get(1).getName()
        );

        verify(supplierRepository)
                .findAll();
    }

    @Test
    void getAllSuppliers_shouldReturnEmptyListWhenNoSuppliers() {

        when(supplierRepository.findAll())
                .thenReturn(List.of());

        List<Supplier> result =
                supplierService.getAllSuppliers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(supplierRepository)
                .findAll();
    }

    // =========================================================
    // getSupplierById()
    // =========================================================

    @Test
    void getSupplierById_shouldReturnSupplier() {

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        Supplier result =
                supplierService.getSupplierById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(
                "ABC Electronics",
                result.getName()
        );
        assertEquals(
                "abc@gmail.com",
                result.getEmail()
        );

        verify(supplierRepository)
                .findById(1L);
    }

    @Test
    void getSupplierById_shouldThrowWhenSupplierNotFound() {

        when(supplierRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> supplierService.getSupplierById(99L)
        );

        verify(supplierRepository)
                .findById(99L);
    }

    // =========================================================
    // updateSupplier()
    // =========================================================

    @Test
    void updateSupplier_shouldUpdateSuccessfully() {

        Supplier updatedSupplier = new Supplier();
        updatedSupplier.setName("Updated Electronics");
        updatedSupplier.setEmail("updated@gmail.com");
        updatedSupplier.setPhone("9999999999");
        updatedSupplier.setAddress(
                "Coimbatore, Tamil Nadu"
        );

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(supplierRepository.save(supplier))
                .thenReturn(supplier);

        Supplier result =
                supplierService.updateSupplier(
                        1L,
                        updatedSupplier
                );

        assertNotNull(result);

        assertEquals(
                "Updated Electronics",
                result.getName()
        );

        assertEquals(
                "updated@gmail.com",
                result.getEmail()
        );

        assertEquals(
                "9999999999",
                result.getPhone()
        );

        assertEquals(
                "Coimbatore, Tamil Nadu",
                result.getAddress()
        );

        assertEquals(
                1L,
                result.getId()
        );

        verify(supplierRepository)
                .findById(1L);

        verify(supplierRepository)
                .save(supplier);
    }

    @Test
    void updateSupplier_shouldThrowWhenSupplierNotFound() {

        Supplier updatedSupplier = new Supplier();
        updatedSupplier.setName("Updated Electronics");
        updatedSupplier.setEmail("updated@gmail.com");
        updatedSupplier.setPhone("9999999999");
        updatedSupplier.setAddress(
                "Coimbatore, Tamil Nadu"
        );

        when(supplierRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> supplierService.updateSupplier(
                        99L,
                        updatedSupplier
                )
        );

        verify(supplierRepository)
                .findById(99L);

        verify(supplierRepository, never())
                .save(any(Supplier.class));
    }

    // =========================================================
    // deleteSupplier()
    // =========================================================

    @Test
    void deleteSupplier_shouldDeleteSuccessfully() {

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        doNothing()
                .when(supplierRepository)
                .delete(supplier);

        supplierService.deleteSupplier(1L);

        verify(supplierRepository)
                .findById(1L);

        verify(supplierRepository)
                .delete(supplier);
    }

    @Test
    void deleteSupplier_shouldThrowWhenSupplierNotFound() {

        when(supplierRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> supplierService.deleteSupplier(99L)
        );

        verify(supplierRepository)
                .findById(99L);

        verify(supplierRepository, never())
                .delete(any(Supplier.class));
    }
}
package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class SupplierRepositoryTest {

    @Autowired
    private SupplierRepository supplierRepository;

    private Supplier createSupplier(
            String name,
            String email) {

        Supplier supplier = new Supplier();

        supplier.setName(name);
        supplier.setEmail(email);
        supplier.setPhone("9876543210");
        supplier.setAddress("Chennai");

        return supplierRepository.save(supplier);
    }

    @Test
    void saveSupplier_success() {

        Supplier supplier =
                createSupplier(
                        "ABC Electronics",
                        "abc@gmail.com"
                );

        assertNotNull(supplier.getId());
        assertEquals(
                "ABC Electronics",
                supplier.getName()
        );
        assertEquals(
                "abc@gmail.com",
                supplier.getEmail()
        );
    }

    @Test
    void findById_success() {

        Supplier supplier =
                createSupplier(
                        "ABC Electronics",
                        "abc@gmail.com"
                );

        Optional<Supplier> result =
                supplierRepository.findById(
                        supplier.getId()
                );

        assertTrue(result.isPresent());
        assertEquals(
                supplier.getId(),
                result.get().getId()
        );
        assertEquals(
                "ABC Electronics",
                result.get().getName()
        );
    }

    @Test
    void findById_notFound() {

        Optional<Supplier> result =
                supplierRepository.findById(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_success() {

        createSupplier(
                "ABC Electronics",
                "abc@gmail.com"
        );

        createSupplier(
                "XYZ Technologies",
                "xyz@gmail.com"
        );

        List<Supplier> result =
                supplierRepository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findAll_empty() {

        List<Supplier> result =
                supplierRepository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void updateSupplier_success() {

        Supplier supplier =
                createSupplier(
                        "ABC Electronics",
                        "abc@gmail.com"
                );

        supplier.setName("ABC Updated");
        supplier.setEmail("updated@gmail.com");
        supplier.setPhone("9999999999");
        supplier.setAddress("Coimbatore");

        Supplier updated =
                supplierRepository.save(supplier);

        assertEquals(
                "ABC Updated",
                updated.getName()
        );
        assertEquals(
                "updated@gmail.com",
                updated.getEmail()
        );
        assertEquals(
                "9999999999",
                updated.getPhone()
        );
        assertEquals(
                "Coimbatore",
                updated.getAddress()
        );
    }

    @Test
    void deleteSupplier_success() {

        Supplier supplier =
                createSupplier(
                        "ABC Electronics",
                        "abc@gmail.com"
                );

        Long id = supplier.getId();

        supplierRepository.delete(supplier);

        Optional<Supplier> result =
                supplierRepository.findById(id);

        assertTrue(result.isEmpty());
    }
}
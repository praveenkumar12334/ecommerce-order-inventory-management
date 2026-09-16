package com.example.ecommerce.service;

import com.example.ecommerce.entity.Supplier;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(
            SupplierRepository supplierRepository) {

        this.supplierRepository = supplierRepository;
    }

    public Supplier createSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier getSupplierById(Long id) {

        return supplierRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found"));
    }

    public Supplier updateSupplier(
            Long id,
            Supplier supplier) {

        Supplier existing =
                getSupplierById(id);

        existing.setName(supplier.getName());
        existing.setEmail(supplier.getEmail());
        existing.setPhone(supplier.getPhone());
        existing.setAddress(supplier.getAddress());

        return supplierRepository.save(existing);
    }

    public void deleteSupplier(Long id) {

        Supplier supplier =
                getSupplierById(id);

        supplierRepository.delete(supplier);
    }
}

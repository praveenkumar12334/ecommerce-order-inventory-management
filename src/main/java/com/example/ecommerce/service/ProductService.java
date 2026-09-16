package com.example.ecommerce.service;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Create product
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by ID
    public Product getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with ID: " + id
                        )
                );
    }

    // Update product
    public Product updateProduct(
            Long id,
            Product updatedProduct) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with ID: " + id
                        )
                );

        product.setName(updatedProduct.getName());
        product.setPrice(updatedProduct.getPrice());
        product.setDescription(updatedProduct.getDescription());
        product.setCategory(updatedProduct.getCategory());

        return productRepository.save(product);
    }

    // Delete product
    public void deleteProduct(Long id) {

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Product not found with ID: " + id
            );
        }

        productRepository.deleteById(id);
    }

    // Search and filter products
    public Page<Product> searchProducts(
            String name,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        // Search by name + price range
        if (name != null && !name.isBlank()
                && minPrice != null
                && maxPrice != null) {

            return productRepository
                    .findByNameContainingIgnoreCaseAndPriceBetween(
                            name,
                            minPrice,
                            maxPrice,
                            pageable
                    );
        }

        // Search by name
        if (name != null && !name.isBlank()) {

            return productRepository
                    .findByNameContainingIgnoreCase(
                            name,
                            pageable
                    );
        }

        // Search by category
        if (category != null && !category.isBlank()) {

            return productRepository
                    .findByCategoryNameIgnoreCase(
                            category,
                            pageable
                    );
        }

        // Search by price range
        if (minPrice != null && maxPrice != null) {

            return productRepository
                    .findByPriceBetween(
                            minPrice,
                            maxPrice,
                            pageable
                    );
        }

        // No filters
        return productRepository.findAll(pageable);
    }
}
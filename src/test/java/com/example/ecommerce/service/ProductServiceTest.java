package com.example.ecommerce.service;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Product updatedProduct;
    private Category category;
    private Pageable pageable;

    @BeforeEach
    void setUp() {

        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(50000.0);
        product.setDescription("Gaming laptop");
        product.setCategory(category);

        updatedProduct = new Product();
        updatedProduct.setName("Updated Laptop");
        updatedProduct.setPrice(55000.0);
        updatedProduct.setDescription("Updated gaming laptop");
        updatedProduct.setCategory(category);

        pageable = PageRequest.of(0, 10);
    }

    // =========================================================
    // saveProduct()
    // =========================================================

    @Test
    void saveProduct_shouldSaveSuccessfully() {

        when(productRepository.save(product))
                .thenReturn(product);

        Product result =
                productService.saveProduct(product);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(50000.0, result.getPrice());

        verify(productRepository)
                .save(product);
    }

    // =========================================================
    // getAllProducts()
    // =========================================================

    @Test
    void getAllProducts_shouldReturnAllProducts() {

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Phone");
        product2.setPrice(25000.0);

        when(productRepository.findAll())
                .thenReturn(List.of(product, product2));

        List<Product> result =
                productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals("Phone", result.get(1).getName());

        verify(productRepository)
                .findAll();
    }

    @Test
    void getAllProducts_shouldReturnEmptyListWhenNoProducts() {

        when(productRepository.findAll())
                .thenReturn(List.of());

        List<Product> result =
                productService.getAllProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository)
                .findAll();
    }

    // =========================================================
    // getProductById()
    // =========================================================

    @Test
    void getProductById_shouldReturnProduct() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result =
                productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(50000.0, result.getPrice());

        verify(productRepository)
                .findById(1L);
    }

    @Test
    void getProductById_shouldThrowWhenProductNotFound() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(1L)
        );

        verify(productRepository)
                .findById(1L);
    }

    // =========================================================
    // updateProduct()
    // =========================================================

    @Test
    void updateProduct_shouldUpdateSuccessfully() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product))
                .thenReturn(product);

        Product result =
                productService.updateProduct(
                        1L,
                        updatedProduct
                );

        assertNotNull(result);

        assertEquals(
                "Updated Laptop",
                result.getName()
        );

        assertEquals(
                55000.0,
                result.getPrice()
        );

        assertEquals(
                "Updated gaming laptop",
                result.getDescription()
        );

        assertEquals(
                category,
                result.getCategory()
        );

        verify(productRepository)
                .findById(1L);

        verify(productRepository)
                .save(product);
    }

    @Test
    void updateProduct_shouldThrowWhenProductNotFound() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(
                        1L,
                        updatedProduct
                )
        );

        verify(productRepository)
                .findById(1L);

        verify(productRepository, never())
                .save(any(Product.class));
    }

    // =========================================================
    // deleteProduct()
    // =========================================================

    @Test
    void deleteProduct_shouldDeleteSuccessfully() {

        when(productRepository.existsById(1L))
                .thenReturn(true);

        doNothing()
                .when(productRepository)
                .deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository)
                .existsById(1L);

        verify(productRepository)
                .deleteById(1L);
    }

    @Test
    void deleteProduct_shouldThrowWhenProductNotFound() {

        when(productRepository.existsById(1L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(1L)
        );

        verify(productRepository)
                .existsById(1L);

        verify(productRepository, never())
                .deleteById(anyLong());
    }

    // =========================================================
    // searchProducts()
    // =========================================================

    @Test
    void searchProducts_shouldSearchByNameAndPrice() {

        Page<Product> page =
                new PageImpl<>(List.of(product));

        BigDecimal minPrice =
                new BigDecimal("40000");

        BigDecimal maxPrice =
                new BigDecimal("60000");

        when(productRepository
                .findByNameContainingIgnoreCaseAndPriceBetween(
                        eq("Laptop"),
                        eq(minPrice),
                        eq(maxPrice),
                        eq(pageable)
                ))
                .thenReturn(page);

        Page<Product> result =
                productService.searchProducts(
                        "Laptop",
                        null,
                        minPrice,
                        maxPrice,
                        pageable
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(productRepository)
                .findByNameContainingIgnoreCaseAndPriceBetween(
                        "Laptop",
                        minPrice,
                        maxPrice,
                        pageable
                );
    }

    @Test
    void searchProducts_shouldSearchByNameOnly() {

        Page<Product> page =
                new PageImpl<>(List.of(product));

        when(productRepository
                .findByNameContainingIgnoreCase(
                        "Laptop",
                        pageable
                ))
                .thenReturn(page);

        Page<Product> result =
                productService.searchProducts(
                        "Laptop",
                        null,
                        null,
                        null,
                        pageable
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(productRepository)
                .findByNameContainingIgnoreCase(
                        "Laptop",
                        pageable
                );
    }

    @Test
    void searchProducts_shouldSearchByCategory() {

        Page<Product> page =
                new PageImpl<>(List.of(product));

        when(productRepository
                .findByCategoryNameIgnoreCase(
                        "Electronics",
                        pageable
                ))
                .thenReturn(page);

        Page<Product> result =
                productService.searchProducts(
                        null,
                        "Electronics",
                        null,
                        null,
                        pageable
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(productRepository)
                .findByCategoryNameIgnoreCase(
                        "Electronics",
                        pageable
                );
    }

    @Test
    void searchProducts_shouldSearchByPriceRange() {

        Page<Product> page =
                new PageImpl<>(List.of(product));

        BigDecimal minPrice =
                new BigDecimal("40000");

        BigDecimal maxPrice =
                new BigDecimal("60000");

        when(productRepository
                .findByPriceBetween(
                        eq(minPrice),
                        eq(maxPrice),
                        eq(pageable)
                ))
                .thenReturn(page);

        Page<Product> result =
                productService.searchProducts(
                        null,
                        null,
                        minPrice,
                        maxPrice,
                        pageable
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(productRepository)
                .findByPriceBetween(
                        minPrice,
                        maxPrice,
                        pageable
                );
    }

    @Test
    void searchProducts_shouldReturnAllWhenNoFilters() {

        Page<Product> page =
                new PageImpl<>(List.of(product));

        when(productRepository.findAll(pageable))
                .thenReturn(page);

        Page<Product> result =
                productService.searchProducts(
                        null,
                        null,
                        null,
                        null,
                        pageable
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(productRepository)
                .findAll(pageable);
    }

    @Test
    void searchProducts_shouldTreatBlankNameAsNoNameFilter() {

        Page<Product> page =
                new PageImpl<>(List.of(product));

        when(productRepository.findAll(pageable))
                .thenReturn(page);

        Page<Product> result =
                productService.searchProducts(
                        "   ",
                        null,
                        null,
                        null,
                        pageable
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(productRepository)
                .findAll(pageable);
    }

    @Test
    void searchProducts_shouldTreatBlankCategoryAsNoCategoryFilter() {

        Page<Product> page =
                new PageImpl<>(List.of(product));

        when(productRepository.findAll(pageable))
                .thenReturn(page);

        Page<Product> result =
                productService.searchProducts(
                        null,
                        "   ",
                        null,
                        null,
                        pageable
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(productRepository)
                .findAll(pageable);
    }
}
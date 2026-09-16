package com.example.ecommerce.service;

import com.example.ecommerce.dto.PurchaseOrderItemRequest;
import com.example.ecommerce.dto.PurchaseOrderRequest;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.PurchaseOrderRepository;
import com.example.ecommerce.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            StockMovementService stockMovementService) {

        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementService = stockMovementService;
    }

    public PurchaseOrder createPurchaseOrder(
            PurchaseOrderRequest request) {

        Supplier supplier =
                supplierRepository.findById(
                        request.getSupplierId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found"));

        PurchaseOrder purchaseOrder =
                new PurchaseOrder();

        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setOrderDate(
                LocalDateTime.now()
        );
        purchaseOrder.setStatus(
                PurchaseOrderStatus.CREATED
        );

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        List<PurchaseOrderItem> items =
                new ArrayList<>();

        for (PurchaseOrderItemRequest itemRequest
                : request.getItems()) {

            Product product =
                    productRepository.findById(
                            itemRequest.getProductId()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Product not found"));

            if (itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Quantity must be greater than zero");
            }

            PurchaseOrderItem item =
                    new PurchaseOrderItem();

            item.setPurchaseOrder(purchaseOrder);
            item.setProduct(product);
            item.setQuantity(
                    itemRequest.getQuantity()
            );
            item.setPurchasePrice(
                    itemRequest.getPurchasePrice()
            );

            BigDecimal itemTotal =
                    itemRequest.getPurchasePrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            itemRequest.getQuantity()
                                    )
                            );

            totalAmount =
                    totalAmount.add(itemTotal);

            items.add(item);
        }

        purchaseOrder.setItems(items);
        purchaseOrder.setTotalAmount(totalAmount);

        return purchaseOrderRepository.save(
                purchaseOrder
        );
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {

        return purchaseOrderRepository.findAll();
    }

    public PurchaseOrder getPurchaseOrderById(
            Long id) {

        return purchaseOrderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Purchase order not found"));
    }

    public PurchaseOrder markAsOrdered(Long id) {

        PurchaseOrder purchaseOrder =
                getPurchaseOrderById(id);

        if (purchaseOrder.getStatus()
                != PurchaseOrderStatus.CREATED) {

            throw new IllegalArgumentException(
                    "Purchase order cannot be marked as ORDERED"
            );
        }

        purchaseOrder.setStatus(
                PurchaseOrderStatus.ORDERED
        );

        return purchaseOrderRepository.save(
                purchaseOrder
        );
    }

    @Transactional
    public PurchaseOrder receivePurchaseOrder(Long id) {

        PurchaseOrder purchaseOrder =
                getPurchaseOrderById(id);

        if (purchaseOrder.getStatus()
                != PurchaseOrderStatus.ORDERED) {

            throw new IllegalArgumentException(
                    "Only ORDERED purchase orders can be received"
            );
        }

        for (PurchaseOrderItem item :
                purchaseOrder.getItems()) {

            Long productId =
                    item.getProduct().getId();

            Inventory inventory =
                    inventoryRepository
                            .findByProductId(productId)
                            .orElseGet(() -> {

                                Inventory newInventory =
                                        new Inventory();

                                newInventory.setProduct(
                                        item.getProduct()
                                );

                                newInventory.setQuantity(0);

                                newInventory.setLowStockThreshold(5);

                                return newInventory;
                            });

            inventory.setQuantity(
                    inventory.getQuantity()
                            + item.getQuantity()
            );

            inventoryRepository.save(inventory);

            stockMovementService.recordMovement(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    StockMovementType.PURCHASE_RECEIVED,
                    purchaseOrder.getId(),
                    "Stock received from purchase order"
            );
        }

        purchaseOrder.setStatus(
                PurchaseOrderStatus.RECEIVED
        );

        return purchaseOrderRepository.save(
                purchaseOrder
        );
    }
}

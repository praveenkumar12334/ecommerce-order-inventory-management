package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.OrderStatusHistory;
import com.example.ecommerce.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OrderStatusHistoryRepositoryTest {

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Order createOrder() {

        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(50000.0);
        product.setDescription("Test product");
        product.setCategory(category);
        product = productRepository.save(product);

        Order order = new Order();
        order.setCustomerName("Test User");
        order.setCustomerEmail("user@gmail.com");
        order.setStatus(OrderStatus.PLACED);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(50000.0);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(1);
        orderItem.setPrice(50000.0);
        orderItem.setOrder(order);

        order.getOrderItems().add(orderItem);

        return orderRepository.save(order);
    }

    private OrderStatusHistory createHistory(
            Order order,
            OrderStatus status) {

        OrderStatusHistory history =
                new OrderStatusHistory();

        history.setOrder(order);
        history.setStatus(status);
        history.setChangedAt(LocalDateTime.now());

        return orderStatusHistoryRepository.save(history);
    }

    @Test
    void saveHistory_success() {

        Order order = createOrder();

        OrderStatusHistory history =
                createHistory(order, OrderStatus.PLACED);

        assertNotNull(history.getId());
        assertEquals(
                OrderStatus.PLACED,
                history.getStatus()
        );
        assertEquals(
                order.getId(),
                history.getOrder().getId()
        );
        assertNotNull(history.getChangedAt());
    }

    @Test
    void findById_success() {

        Order order = createOrder();

        OrderStatusHistory history =
                createHistory(order, OrderStatus.PLACED);

        Optional<OrderStatusHistory> result =
                orderStatusHistoryRepository.findById(
                        history.getId()
                );

        assertTrue(result.isPresent());
        assertEquals(
                history.getId(),
                result.get().getId()
        );
        assertEquals(
                OrderStatus.PLACED,
                result.get().getStatus()
        );
    }

    @Test
    void findById_notFound() {

        Optional<OrderStatusHistory> result =
                orderStatusHistoryRepository.findById(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_success() {

        Order order = createOrder();

        createHistory(order, OrderStatus.PLACED);
        createHistory(order, OrderStatus.CONFIRMED);

        List<OrderStatusHistory> result =
                orderStatusHistoryRepository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findAll_empty() {

        List<OrderStatusHistory> result =
                orderStatusHistoryRepository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void updateHistory_success() {

        Order order = createOrder();

        OrderStatusHistory history =
                createHistory(order, OrderStatus.PLACED);

        history.setStatus(OrderStatus.CONFIRMED);

        OrderStatusHistory updated =
                orderStatusHistoryRepository.save(history);

        assertEquals(
                OrderStatus.CONFIRMED,
                updated.getStatus()
        );
    }

    @Test
    void deleteHistory_success() {

        Order order = createOrder();

        OrderStatusHistory history =
                createHistory(order, OrderStatus.PLACED);

        Long id = history.getId();

        orderStatusHistoryRepository.delete(history);

        Optional<OrderStatusHistory> result =
                orderStatusHistoryRepository.findById(id);

        assertTrue(result.isEmpty());
    }
}
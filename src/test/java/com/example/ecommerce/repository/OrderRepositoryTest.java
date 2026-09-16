package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Product;
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
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product createProduct(String name, double price) {

        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescription("Test product");
        product.setCategory(category);

        return productRepository.save(product);
    }

    private Order createOrder(
            String email,
            OrderStatus status) {

        Product product =
                createProduct(
                        "Test Product",
                        1000.0
                );

        Order order = new Order();

        order.setCustomerName("Test Customer");
        order.setCustomerEmail(email);
        order.setStatus(status);
        order.setTotalAmount(1000.0);

        OrderItem orderItem = new OrderItem();

        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(1);
        orderItem.setPrice(1000.0);

        order.getOrderItems().add(orderItem);

        return orderRepository.save(order);
    }

    @Test
    void countByStatus_success() {

        createOrder(
                "user1@gmail.com",
                OrderStatus.PLACED
        );

        createOrder(
                "user2@gmail.com",
                OrderStatus.PLACED
        );

        createOrder(
                "user3@gmail.com",
                OrderStatus.CONFIRMED
        );

        long placedCount =
                orderRepository.countByStatus(
                        OrderStatus.PLACED
                );

        long confirmedCount =
                orderRepository.countByStatus(
                        OrderStatus.CONFIRMED
                );

        assertEquals(2, placedCount);
        assertEquals(1, confirmedCount);
    }

    @Test
    void countByStatus_noOrders_returnsZero() {

        long count =
                orderRepository.countByStatus(
                        OrderStatus.DELIVERED
                );

        assertEquals(0, count);
    }

    @Test
    void findByStatus_success() {

        createOrder(
                "user1@gmail.com",
                OrderStatus.PLACED
        );

        createOrder(
                "user2@gmail.com",
                OrderStatus.PLACED
        );

        createOrder(
                "user3@gmail.com",
                OrderStatus.SHIPPED
        );

        List<Order> result =
                orderRepository.findByStatus(
                        OrderStatus.PLACED
                );

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(
                                order -> order.getStatus()
                                        == OrderStatus.PLACED
                        )
        );
    }

    @Test
    void findByStatus_noOrders_returnsEmpty() {

        List<Order> result =
                orderRepository.findByStatus(
                        OrderStatus.CANCELLED
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByCustomerEmail_success() {

        createOrder(
                "user@gmail.com",
                OrderStatus.PLACED
        );

        createOrder(
                "user@gmail.com",
                OrderStatus.CONFIRMED
        );

        createOrder(
                "other@gmail.com",
                OrderStatus.PLACED
        );

        List<Order> result =
                orderRepository.findByCustomerEmail(
                        "user@gmail.com"
                );

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(
                                order -> order.getCustomerEmail()
                                        .equals("user@gmail.com")
                        )
        );
    }

    @Test
    void findByCustomerEmail_notFound_returnsEmpty() {

        List<Order> result =
                orderRepository.findByCustomerEmail(
                        "unknown@gmail.com"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findCustomerOrdersContainingProduct_success() {

        Product laptop =
                createProduct("Laptop", 50000.0);

        Product mouse =
                createProduct("Mouse", 1000.0);

        Order order =
                createOrder(
                        "user@gmail.com",
                        OrderStatus.DELIVERED
                );

        OrderItem orderItem = new OrderItem();

        orderItem.setOrder(order);
        orderItem.setProduct(laptop);
        orderItem.setQuantity(1);
        orderItem.setPrice(50000.0);

        order.getOrderItems().add(orderItem);

        orderRepository.save(order);

        List<Order> result =
                orderRepository
                        .findCustomerOrdersContainingProduct(
                                "user@gmail.com",
                                laptop.getId(),
                                OrderStatus.DELIVERED
                        );

        assertEquals(1, result.size());

        assertEquals(
                order.getId(),
                result.getFirst().getId()
        );

        assertEquals(
                "user@gmail.com",
                result.getFirst().getCustomerEmail()
        );
    }

    @Test
    void findCustomerOrdersContainingProduct_wrongProduct_returnsEmpty() {

        Product laptop =
                createProduct("Laptop", 50000.0);

        Product mouse =
                createProduct("Mouse", 1000.0);

        Order order =
                createOrder(
                        "user@gmail.com",
                        OrderStatus.DELIVERED
                );

        OrderItem orderItem = new OrderItem();

        orderItem.setOrder(order);
        orderItem.setProduct(laptop);
        orderItem.setQuantity(1);
        orderItem.setPrice(50000.0);

        order.getOrderItems().add(orderItem);

        orderRepository.save(order);

        List<Order> result =
                orderRepository
                        .findCustomerOrdersContainingProduct(
                                "user@gmail.com",
                                mouse.getId(),
                                OrderStatus.DELIVERED
                        );

        assertTrue(result.isEmpty());
    }

    @Test
    void findCustomerOrdersContainingProduct_wrongEmail_returnsEmpty() {

        Product laptop =
                createProduct("Laptop", 50000.0);

        Order order =
                createOrder(
                        "user@gmail.com",
                        OrderStatus.DELIVERED
                );

        OrderItem orderItem = new OrderItem();

        orderItem.setOrder(order);
        orderItem.setProduct(laptop);
        orderItem.setQuantity(1);
        orderItem.setPrice(50000.0);

        order.getOrderItems().add(orderItem);

        orderRepository.save(order);

        List<Order> result =
                orderRepository
                        .findCustomerOrdersContainingProduct(
                                "other@gmail.com",
                                laptop.getId(),
                                OrderStatus.DELIVERED
                        );

        assertTrue(result.isEmpty());
    }

    @Test
    void findCustomerOrdersContainingProduct_wrongStatus_returnsEmpty() {

        Product laptop =
                createProduct("Laptop", 50000.0);

        Order order =
                createOrder(
                        "user@gmail.com",
                        OrderStatus.PLACED
                );

        OrderItem orderItem = new OrderItem();

        orderItem.setOrder(order);
        orderItem.setProduct(laptop);
        orderItem.setQuantity(1);
        orderItem.setPrice(50000.0);

        order.getOrderItems().add(orderItem);

        orderRepository.save(order);

        List<Order> result =
                orderRepository
                        .findCustomerOrdersContainingProduct(
                                "user@gmail.com",
                                laptop.getId(),
                                OrderStatus.DELIVERED
                        );

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_success() {

        Order order =
                createOrder(
                        "user@gmail.com",
                        OrderStatus.PLACED
                );

        Optional<Order> result =
                orderRepository.findById(
                        order.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                order.getId(),
                result.get().getId()
        );

        assertEquals(
                "user@gmail.com",
                result.get().getCustomerEmail()
        );
    }

    @Test
    void findById_notFound() {

        Optional<Order> result =
                orderRepository.findById(999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteOrder_success() {

        Order order =
                createOrder(
                        "user@gmail.com",
                        OrderStatus.CANCELLED
                );

        Long id = order.getId();

        orderRepository.deleteById(id);

        assertTrue(
                orderRepository.findById(id).isEmpty()
        );
    }
}
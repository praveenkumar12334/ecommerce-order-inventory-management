package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Product;

import com.example.ecommerce.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User createUser(
            String name,
            String email) {

        User user = new User();

        user.setName(name);
        user.setEmail(email);
        user.setPassword("password123");
        user.setRole(Role.CUSTOMER);

        return userRepository.save(user);
    }

    private Order createOrder(
            String email,
            OrderStatus status) {

        Category category = new Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(5000.0);
        product.setDescription("Test product");
        product.setCategory(category);
        product = productRepository.save(product);

        Order order = new Order();

        order.setCustomerName("Test Customer");
        order.setCustomerEmail(email);
        order.setStatus(status);
        order.setTotalAmount(5000.0);

        OrderItem orderItem = new OrderItem();

        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(1);
        orderItem.setPrice(5000.0);

        order.getOrderItems().add(orderItem);

        return orderRepository.save(order);
    }

    @Test
    void savePayment_success() {

        User user =
                createUser(
                        "Test User",
                        "user@gmail.com"
                );

        Order order =
                createOrder(
                        user.getEmail(),
                        OrderStatus.CONFIRMED
                );

        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setAmount(5000.0);
        payment.setTransactionId("TXN-12345");

        Payment saved =
                paymentRepository.save(payment);

        assertNotNull(saved.getId());

        assertEquals(
                order.getId(),
                saved.getOrder().getId()
        );

        assertEquals(
                5000.0,
                saved.getAmount()
        );

        assertEquals(
                PaymentMethod.UPI,
                saved.getPaymentMethod()
        );

        assertEquals(
                "TXN-12345",
                saved.getTransactionId()
        );
    }

    @Test
    void findByOrderId_success() {

        User user =
                createUser(
                        "Test User",
                        "user@gmail.com"
                );

        Order order =
                createOrder(
                        user.getEmail(),
                        OrderStatus.CONFIRMED
                );

        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setAmount(5000.0);
        payment.setTransactionId("TXN-10001");

        paymentRepository.save(payment);

        Optional<Payment> result =
                paymentRepository.findByOrderId(
                        order.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                order.getId(),
                result.get().getOrder().getId()
        );

        assertEquals(
                "TXN-10001",
                result.get().getTransactionId()
        );
    }

    @Test
    void findByOrderId_notFound() {

        Optional<Payment> result =
                paymentRepository.findByOrderId(
                        999999L
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByOrderId_returnsCorrectPayment() {

        User user1 =
                createUser(
                        "User One",
                        "user1@gmail.com"
                );

        User user2 =
                createUser(
                        "User Two",
                        "user2@gmail.com"
                );

        Order order1 =
                createOrder(
                        user1.getEmail(),
                        OrderStatus.CONFIRMED
                );

        Order order2 =
                createOrder(
                        user2.getEmail(),
                        OrderStatus.CONFIRMED
                );

        Payment payment1 = new Payment();

        payment1.setOrder(order1);
        payment1.setPaymentMethod(PaymentMethod.UPI);
        payment1.setAmount(5000.0);
        payment1.setTransactionId("TXN-11111");

        Payment payment2 = new Payment();

        payment2.setOrder(order2);
        payment2.setPaymentMethod(PaymentMethod.UPI);
        payment2.setAmount(7000.0);
        payment2.setTransactionId("TXN-22222");

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        Optional<Payment> result =
                paymentRepository.findByOrderId(
                        order1.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                "TXN-11111",
                result.get().getTransactionId()
        );

        assertEquals(
                order1.getId(),
                result.get().getOrder().getId()
        );
    }

    @Test
    void findById_success() {

        User user =
                createUser(
                        "Test User",
                        "user@gmail.com"
                );

        Order order =
                createOrder(
                        user.getEmail(),
                        OrderStatus.CONFIRMED
                );

        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setAmount(5000.0);
        payment.setTransactionId("TXN-30001");

        Payment saved =
                paymentRepository.save(payment);

        Optional<Payment> result =
                paymentRepository.findById(
                        saved.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                saved.getId(),
                result.get().getId()
        );
    }

    @Test
    void deletePayment_success() {

        User user =
                createUser(
                        "Test User",
                        "user@gmail.com"
                );

        Order order =
                createOrder(
                        user.getEmail(),
                        OrderStatus.CONFIRMED
                );

        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setAmount(5000.0);
        payment.setTransactionId("TXN-40001");

        Payment saved =
                paymentRepository.save(payment);

        Long id = saved.getId();

        paymentRepository.deleteById(id);

        assertTrue(
                paymentRepository.findById(id).isEmpty()
        );
    }
}
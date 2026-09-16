package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Delivery;
import com.example.ecommerce.entity.DeliveryStatus;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class DeliveryRepositoryTest {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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
    void saveDelivery_success() {

        Order order =
                createOrder(
                        "user@gmail.com",
                        OrderStatus.CONFIRMED
                );

        Delivery delivery = new Delivery();

        delivery.setOrder(order);
        delivery.setDeliveryAddress("123 Main Street");
        delivery.setDeliveryPerson("John");
        delivery.setTrackingNumber("TRK-12345678");
        delivery.setStatus(DeliveryStatus.NOT_ASSIGNED);
        delivery.setEstimatedDeliveryDate(
                LocalDateTime.now().plusDays(5)
        );

        Delivery saved =
                deliveryRepository.save(delivery);

        assertNotNull(saved.getId());

        assertEquals(
                order.getId(),
                saved.getOrder().getId()
        );

        assertEquals(
                "TRK-12345678",
                saved.getTrackingNumber()
        );

        assertEquals(
                DeliveryStatus.NOT_ASSIGNED,
                saved.getStatus()
        );
    }

    @Test
    void findByOrderId_success() {

        Order order =
                createOrder(
                        "user@gmail.com",
                        OrderStatus.CONFIRMED
                );

        Delivery delivery = new Delivery();

        delivery.setOrder(order);
        delivery.setDeliveryAddress("123 Main Street");
        delivery.setDeliveryPerson("John");
        delivery.setTrackingNumber("TRK-11111111");
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setEstimatedDeliveryDate(
                LocalDateTime.now().plusDays(5)
        );

        deliveryRepository.save(delivery);

        Optional<Delivery> result =
                deliveryRepository.findByOrderId(
                        order.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                order.getId(),
                result.get().getOrder().getId()
        );

        assertEquals(
                "TRK-11111111",
                result.get().getTrackingNumber()
        );

        assertEquals(
                DeliveryStatus.ASSIGNED,
                result.get().getStatus()
        );
    }

    @Test
    void findByOrderId_notFound() {

        Optional<Delivery> result =
                deliveryRepository.findByOrderId(
                        999999L
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByOrderId_returnsCorrectDelivery() {

        Order order1 =
                createOrder(
                        "user1@gmail.com",
                        OrderStatus.CONFIRMED
                );

        Order order2 =
                createOrder(
                        "user2@gmail.com",
                        OrderStatus.CONFIRMED
                );

        Delivery delivery1 = new Delivery();
        delivery1.setOrder(order1);
        delivery1.setDeliveryAddress("Address One");
        delivery1.setDeliveryPerson("Person One");
        delivery1.setTrackingNumber("TRK-11111111");
        delivery1.setStatus(DeliveryStatus.ASSIGNED);
        delivery1.setEstimatedDeliveryDate(
                LocalDateTime.now().plusDays(5)
        );

        Delivery delivery2 = new Delivery();
        delivery2.setOrder(order2);
        delivery2.setDeliveryAddress("Address Two");
        delivery2.setDeliveryPerson("Person Two");
        delivery2.setTrackingNumber("TRK-22222222");
        delivery2.setStatus(DeliveryStatus.OUT_FOR_DELIVERY);
        delivery2.setEstimatedDeliveryDate(
                LocalDateTime.now().plusDays(3)
        );

        deliveryRepository.save(delivery1);
        deliveryRepository.save(delivery2);

        Optional<Delivery> result =
                deliveryRepository.findByOrderId(
                        order2.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                "TRK-22222222",
                result.get().getTrackingNumber()
        );

        assertEquals(
                order2.getId(),
                result.get().getOrder().getId()
        );
    }

    @Test
    void findById_success() {

        Order order =
                createOrder(
                        "user@gmail.com",
                        OrderStatus.CONFIRMED
                );

        Delivery delivery = new Delivery();

        delivery.setOrder(order);
        delivery.setDeliveryAddress("123 Main Street");
        delivery.setDeliveryPerson("John");
        delivery.setTrackingNumber("TRK-33333333");
        delivery.setStatus(DeliveryStatus.NOT_ASSIGNED);
        delivery.setEstimatedDeliveryDate(
                LocalDateTime.now().plusDays(5)
        );

        Delivery saved =
                deliveryRepository.save(delivery);

        Optional<Delivery> result =
                deliveryRepository.findById(
                        saved.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                saved.getId(),
                result.get().getId()
        );
    }

    @Test
    void deleteDelivery_success() {

        Order order =
                createOrder(
                        "user@gmail.com",
                        OrderStatus.CONFIRMED
                );

        Delivery delivery = new Delivery();

        delivery.setOrder(order);
        delivery.setDeliveryAddress("123 Main Street");
        delivery.setDeliveryPerson("John");
        delivery.setTrackingNumber("TRK-44444444");
        delivery.setStatus(DeliveryStatus.NOT_ASSIGNED);
        delivery.setEstimatedDeliveryDate(
                LocalDateTime.now().plusDays(5)
        );

        Delivery saved =
                deliveryRepository.save(delivery);

        Long id = saved.getId();

        deliveryRepository.deleteById(id);

        assertTrue(
                deliveryRepository.findById(id).isEmpty()
        );
    }
}
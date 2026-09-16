package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Address;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    private User createUser(String email) {

        User user = new User();

        user.setName("Test User");
        user.setEmail(email);
        user.setPassword(
                new BCryptPasswordEncoder().encode("password")
        );
        user.setRole(Role.CUSTOMER);

        return userRepository.save(user);
    }

    private Address createAddress(
            User user,
            String city) {

        Address address = new Address();

        address.setFullName(user.getName());
        address.setPhone("9876543210");
        address.setAddressLine("123 Main Street");
        address.setCity(city);
        address.setState("Tamil Nadu");
        address.setPincode("600001");
        address.setUser(user);

        return addressRepository.save(address);
    }

    @Test
    void saveAddress_success() {

        User user = createUser("user@gmail.com");

        Address address =
                createAddress(user, "Chennai");

        assertNotNull(address.getId());
        assertEquals("Chennai", address.getCity());
        assertEquals(user.getId(), address.getUser().getId());
    }

    @Test
    void findByUserId_success() {

        User user = createUser("user@gmail.com");

        createAddress(user, "Chennai");
        createAddress(user, "Salem");

        List<Address> result =
                addressRepository.findByUserId(user.getId());

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(address ->
                                address.getUser()
                                        .getId()
                                        .equals(user.getId()))
        );
    }

    @Test
    void findByUserId_onlyReturnsRequestedUser() {

        User user1 = createUser("user1@gmail.com");
        User user2 = createUser("user2@gmail.com");

        createAddress(user1, "Chennai");
        createAddress(user2, "Salem");

        List<Address> result =
                addressRepository.findByUserId(user1.getId());

        assertEquals(1, result.size());
        assertEquals(
                user1.getId(),
                result.getFirst().getUser().getId()
        );
        assertEquals(
                "Chennai",
                result.getFirst().getCity()
        );
    }

    @Test
    void findByUserId_noResult() {

        User user = createUser("user@gmail.com");

        List<Address> result =
                addressRepository.findByUserId(user.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdAndUserId_success() {

        User user = createUser("user@gmail.com");

        Address address =
                createAddress(user, "Chennai");

        Optional<Address> result =
                addressRepository.findByIdAndUserId(
                        address.getId(),
                        user.getId()
                );

        assertTrue(result.isPresent());
        assertEquals(
                address.getId(),
                result.get().getId()
        );
        assertEquals(
                user.getId(),
                result.get().getUser().getId()
        );
    }

    @Test
    void findByIdAndUserId_wrongUser_returnsEmpty() {

        User owner = createUser("owner@gmail.com");
        User otherUser = createUser("other@gmail.com");

        Address address =
                createAddress(owner, "Chennai");

        Optional<Address> result =
                addressRepository.findByIdAndUserId(
                        address.getId(),
                        otherUser.getId()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdAndUserId_wrongAddress_returnsEmpty() {

        User user = createUser("user@gmail.com");

        createAddress(user, "Chennai");

        Optional<Address> result =
                addressRepository.findByIdAndUserId(
                        999999L,
                        user.getId()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_success() {

        User user = createUser("user@gmail.com");

        Address address =
                createAddress(user, "Chennai");

        Optional<Address> result =
                addressRepository.findById(address.getId());

        assertTrue(result.isPresent());
        assertEquals(
                address.getId(),
                result.get().getId()
        );
    }

    @Test
    void deleteAddress_success() {

        User user = createUser("user@gmail.com");

        Address address =
                createAddress(user, "Chennai");

        Long id = address.getId();

        addressRepository.delete(address);

        Optional<Address> result =
                addressRepository.findById(id);

        assertTrue(result.isEmpty());
    }
}
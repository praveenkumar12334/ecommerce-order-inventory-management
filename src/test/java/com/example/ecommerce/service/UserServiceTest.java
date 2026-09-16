package com.example.ecommerce.service;

import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("user@gmail.com");
        user.setPassword("password123");
        user.setRole(Role.ADMIN);
    }

    // =========================================================
    // saveUser()
    // =========================================================

    @Test
    void saveUser_shouldEncodePasswordAndSaveUser() {

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(user))
                .thenReturn(user);

        User result =
                userService.saveUser(user);

        assertNotNull(result);

        assertEquals(
                "encodedPassword",
                result.getPassword()
        );

        assertEquals(
                Role.CUSTOMER,
                result.getRole()
        );

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(user);
    }

    @Test
    void saveUser_shouldAlwaysAssignCustomerRole() {

        user.setRole(Role.ADMIN);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(user))
                .thenReturn(user);

        User result =
                userService.saveUser(user);

        assertEquals(
                Role.CUSTOMER,
                result.getRole()
        );

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(user);
    }

    @Test
    void saveUser_shouldNotKeepPlainTextPassword() {

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(user))
                .thenReturn(user);

        User result =
                userService.saveUser(user);

        assertNotEquals(
                "password123",
                result.getPassword()
        );

        assertEquals(
                "encodedPassword",
                result.getPassword()
        );
    }

    // =========================================================
    // findByEmailOrThrow()
    // =========================================================

    @Test
    void findByEmailOrThrow_shouldReturnUserWhenFound() {

        when(userRepository.findByEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        User result =
                userService.findByEmailOrThrow(
                        "user@gmail.com"
                );

        assertNotNull(result);
        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "user@gmail.com",
                result.getEmail()
        );

        verify(userRepository)
                .findByEmail("user@gmail.com");
    }

    @Test
    void findByEmailOrThrow_shouldThrowWhenUserNotFound() {

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.findByEmailOrThrow(
                        "unknown@gmail.com"
                )
        );

        verify(userRepository)
                .findByEmail("unknown@gmail.com");
    }

    @Test
    void findByEmailOrThrow_shouldNotSaveUserWhenNotFound() {

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.findByEmailOrThrow(
                        "unknown@gmail.com"
                )
        );

        verify(userRepository, never())
                .save(any(User.class));
    }
}
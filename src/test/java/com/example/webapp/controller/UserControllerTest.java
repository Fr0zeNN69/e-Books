package com.example.webapp.controller;

import com.example.webapp.model.Book;
import com.example.webapp.model.User;
import com.example.webapp.repository.BookRepository;
import com.example.webapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Model model;

    @InjectMocks
    private UserController userController;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUsername("testuser");
        user.setPassword("plaintextpassword");
        book = new Book();
        book.setId("1");
    }

    // Test pentru înregistrarea unui utilizator nou
    @Test
    void testRegisterUser_Success() {
        when(passwordEncoder.encode("plaintextpassword")).thenReturn("encodedpassword");
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = userController.registerUser(user, bindingResult, model);

        verify(userRepository).save(any(User.class));
        assertEquals("redirect:/login", result);
    }

    @Test
    void testRegisterUser_WithErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = userController.registerUser(user, bindingResult, model);

        verify(userRepository, never()).save(any(User.class));
        assertEquals("register", result);
    }

    // Test pentru adăugarea unei cărți la favorite
    @Test
    void testAddFavorite_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById("1")).thenReturn(Optional.of(book));

        ResponseEntity<String> response = userController.addFavorite("1", authentication);

        verify(userRepository).save(user);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cartea a fost adăugată la favorite", response.getBody());
    }

    @Test
    void testAddFavorite_UnauthenticatedUser() {
        when(authentication.isAuthenticated()).thenReturn(false);

        ResponseEntity<String> response = userController.addFavorite("1", authentication);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Utilizator neautentificat", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAddFavorite_BookAlreadyFavorited() {
        user.getFavoriteBooks().add(book);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById("1")).thenReturn(Optional.of(book));

        ResponseEntity<String> response = userController.addFavorite("1", authentication);

        verify(userRepository, never()).save(user);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cartea a fost adăugată la favorite", response.getBody());
    }

    // Test pentru eliminarea unei cărți din favorite
    @Test
    void testRemoveFavorite_Success() {
        user.getFavoriteBooks().add(book);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById("1")).thenReturn(Optional.of(book));

        ResponseEntity<String> response = userController.removeFavorite("1", authentication);

        verify(userRepository).save(user);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cartea a fost eliminată din favorite", response.getBody());
    }

    @Test
    void testRemoveFavorite_BookNotInFavorites() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById("1")).thenReturn(Optional.of(book));

        ResponseEntity<String> response = userController.removeFavorite("1", authentication);

        verify(userRepository, never()).save(user);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cartea a fost eliminată din favorite", response.getBody());
    }

    // Test pentru listarea tuturor utilizatorilor
    @Test
    void testGetAllUsers() {
        List<User> users = Arrays.asList(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userController.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    // Test pentru adăugarea unui utilizator nou prin API
    @Test
    void testAddUser() {
        when(passwordEncoder.encode("plaintextpassword")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userController.addUser(user);

        assertEquals("encodedpassword", result.getPassword());
        verify(userRepository).save(user);
    }
}

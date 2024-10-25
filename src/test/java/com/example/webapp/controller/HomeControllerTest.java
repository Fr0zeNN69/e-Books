package com.example.webapp.controller;

import com.example.webapp.model.Book;
import com.example.webapp.model.Review;
import com.example.webapp.model.User;
import com.example.webapp.repository.BookRepository;
import com.example.webapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HomeControllerTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @InjectMocks
    private HomeController homeController;

    private User user;
    private Set<Book> favoriteBooks;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up user and favorite books
        user = new User();
        user.setUsername("testuser");

        favoriteBooks = new HashSet<>();
        Book book1 = new Book();
        book1.setTitle("Book 1");

        Book book2 = new Book();
        book2.setTitle("Book 2");

        favoriteBooks.add(book1);
        favoriteBooks.add(book2);

        user.setFavoriteBooks(favoriteBooks);
    }

    @Test
    void testHome_WithAuthenticatedUserAndFavoriteBooks() {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        String viewName = homeController.home(authentication, model);

        verify(model).addAttribute("currentUsername", "testuser");
        verify(model).addAttribute("books", favoriteBooks);
        verify(model, never()).addAttribute("message", "You have no favorite books.");
        assertEquals("home", viewName);
    }

    @Test
    void testHome_WithAuthenticatedUserAndNoFavoriteBooks() {
        user.setFavoriteBooks(new HashSet<>()); // No favorite books
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        String viewName = homeController.home(authentication, model);

        verify(model).addAttribute("currentUsername", "testuser");
        verify(model).addAttribute("books", new HashSet<>()); // Empty set of favorite books
        verify(model).addAttribute("message", "You have no favorite books.");
        assertEquals("home", viewName);
    }

    @Test
    void testHome_WithUnauthenticatedUser() {
        when(authentication.getName()).thenReturn(null);

        String viewName = homeController.home(null, model);

        verify(model).addAttribute("currentUsername", "Guest");
        verify(model).addAttribute("message", "You need to log in to see your favorite books.");
        assertEquals("home", viewName);
    }

    @Test
    void testHome_WithNullAuthentication() {
        String viewName = homeController.home(null, model);

        verify(model).addAttribute("currentUsername", "Guest");
        verify(model).addAttribute("message", "You need to log in to see your favorite books.");
        assertEquals("home", viewName);
    }
}

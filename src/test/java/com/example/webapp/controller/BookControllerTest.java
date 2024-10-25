package com.example.webapp.controller;

import com.example.webapp.model.Book;
import com.example.webapp.model.Review;
import com.example.webapp.model.User;
import com.example.webapp.repository.BookRepository;
import com.example.webapp.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.client.RestTemplate;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;


import java.util.Set;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BookControllerTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookController bookController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSearchBooksWithEmptyQuery() {
        // When no query is provided, the search should not be active
        String result = bookController.searchBooks("", null, "none", "asc", null, model, authentication);
        assertEquals("book_search_results", result);
    }

    @Test
    void testGetTopBooks() {
        // Prepare data
        List<Book> books = new ArrayList<>();
        Book book = new Book();
        book.setTitle("Test Book");

        // Create a Set of reviews and convert it to a List
        Set<Review> reviewSet = new HashSet<>();
        Review review = new Review();
        review.setRating(4);
        reviewSet.add(review);

        // Convert Set to List and set it to the book
        book.setReviews(new ArrayList<>(reviewSet));
        books.add(book);

        when(bookRepository.findAll()).thenReturn(books);

        // Call the method
        String result = bookController.getTopBooks(model, authentication);

        // Verify that the right data was added to the model
        verify(bookRepository, times(1)).findAll();
        verify(model, times(1)).addAttribute(eq("books"), anyList());
        assertEquals("top_books", result);
    }

    @Test
    void testGetHottestTopics() {
        // Prepare data
        List<Book> books = new ArrayList<>();
        Book book = new Book();
        book.setTitle("Test Book");

        // Create a Set of reviews and convert it to a List
        Set<Review> reviewSet = new HashSet<>();
        reviewSet.add(new Review());
        reviewSet.add(new Review()); // Add two reviews

        // Convert Set to List and set it to the book
        book.setReviews(new ArrayList<>(reviewSet));
        books.add(book);

        when(bookRepository.findAll()).thenReturn(books);

        // Call the method
        String result = bookController.getHottestTopics(model, authentication);

        // Verify that the right data was added to the model
        verify(bookRepository, times(1)).findAll();
        verify(model, times(1)).addAttribute(eq("books"), anyList());
        assertEquals("hottest_topics", result);
    }

}
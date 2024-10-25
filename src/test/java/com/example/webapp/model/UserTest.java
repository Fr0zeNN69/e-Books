package com.example.webapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("test user");
        user.setUsername("testuser");
        user.setPassword("securepassword");
        user.setBio("This is a test bio");
        user.setProfileImageUrl("http://example.com/profile.jpg");
        user.setRegistrationDate(LocalDate.of(2023, 10, 15));
    }

    @Test
    void testGetAndSetId() {
        user.setId(123L);
        assertEquals(123L, user.getId());
    }

    @Test
    void testGetAndSetName() {
        user.setName("test");
        assertEquals("test", user.getName());
    }

    @Test
    void testGetAndSetUsername() {
        user.setUsername("teest");
        assertEquals("teest", user.getUsername());
    }

    @Test
    void testGetAndSetPassword() {
        user.setPassword("newsecurepassword");
        assertEquals("newsecurepassword", user.getPassword());
    }

    @Test
    void testGetAndSetBio() {
        user.setBio("Updated bio text");
        assertEquals("Updated bio text", user.getBio());
    }

    @Test
    void testGetAndSetProfileImageUrl() {
        user.setProfileImageUrl("http://example.com/newprofile.jpg");
        assertEquals("http://example.com/newprofile.jpg", user.getProfileImageUrl());
    }

    @Test
    void testGetAndSetRegistrationDate() {
        LocalDate newDate = LocalDate.of(2022, 8, 10);
        user.setRegistrationDate(newDate);
        assertEquals(newDate, user.getRegistrationDate());
    }

    @Test
    void testGetAndSetFavoriteBooks() {
        Book book1 = new Book();
        book1.setId("book1");
        book1.setTitle("Book One");

        Book book2 = new Book();
        book2.setId("book2");
        book2.setTitle("Book Two");

        Set<Book> favoriteBooks = new HashSet<>();
        favoriteBooks.add(book1);
        favoriteBooks.add(book2);

        user.setFavoriteBooks(favoriteBooks);

        assertEquals(2, user.getFavoriteBooks().size());
        assertTrue(user.getFavoriteBooks().contains(book1));
        assertTrue(user.getFavoriteBooks().contains(book2));
    }

    @Test
    void testAddFavoriteBook() {
        Book book = new Book();
        book.setId("book1");
        book.setTitle("Sample Book");

        user.getFavoriteBooks().add(book);

        assertEquals(1, user.getFavoriteBooks().size());
        assertTrue(user.getFavoriteBooks().contains(book));
    }

    @Test
    void testRemoveFavoriteBook() {
        Book book = new Book();
        book.setId("book1");
        book.setTitle("Sample Book");

        user.getFavoriteBooks().add(book);
        user.getFavoriteBooks().remove(book);

        assertTrue(user.getFavoriteBooks().isEmpty());
    }
}

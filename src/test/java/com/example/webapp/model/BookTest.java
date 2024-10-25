package com.example.webapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookTest {

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId("1");
        book.setTitle("Test Book");
        book.setAuthors("Author One, Author Two");
        book.setDescription("This is a test description.");
        book.setCategories("Category One, Category Two");
        book.setPublishDate(LocalDate.of(2023, 1, 1));
    }

    @Test
    void testGetAndSetId() {
        book.setId("123");
        assertEquals("123", book.getId());
    }

    @Test
    void testGetAndSetTitle() {
        book.setTitle("New Title");
        assertEquals("New Title", book.getTitle());
    }

    @Test
    void testGetAndSetAuthors() {
        book.setAuthors("New Author");
        assertEquals("New Author", book.getAuthors());
    }

    @Test
    void testGetAndSetDescription() {
        book.setDescription("Updated description.");
        assertEquals("Updated description.", book.getDescription());
    }

    @Test
    void testGetAndSetCategories() {
        book.setCategories("Updated Category");
        assertEquals("Updated Category", book.getCategories());
    }

    @Test
    void testGetAndSetPublishDate() {
        LocalDate newDate = LocalDate.of(2022, 5, 15);
        book.setPublishDate(newDate);
        assertEquals(newDate, book.getPublishDate());
    }

    @Test
    void testGetAndSetReviews() {
        List<Review> reviews = new ArrayList<>();
        Review review = new Review();
        review.setReviewText("Sample Review");
        reviews.add(review);

        book.setReviews(reviews);
        assertEquals(1, book.getReviews().size());
        assertEquals("Sample Review", book.getReviews().get(0).getReviewText());
    }

    @Test
    void testAddReview() {
        Review review1 = new Review();
        review1.setReviewText("First review");
        Review review2 = new Review();
        review2.setReviewText("Second review");

        book.getReviews().add(review1);
        book.getReviews().add(review2);

        assertEquals(2, book.getReviews().size());
        assertEquals("First review", book.getReviews().get(0).getReviewText());
        assertEquals("Second review", book.getReviews().get(1).getReviewText());
    }

    @Test
    void testGetAndSetAverageRating() {
        book.setAverageRating(4.5);
        assertEquals(4.5, book.getAverageRating());
    }

    @Test
    void testCalculateAverageRating() {
        Review review1 = new Review();
        review1.setRating(4);
        Review review2 = new Review();
        review2.setRating(5);

        book.getReviews().add(review1);
        book.getReviews().add(review2);

        double averageRating = book.getReviews().stream().mapToInt(Review::getRating).average().orElse(0.0);
        book.setAverageRating(averageRating);

        assertEquals(4.5, book.getAverageRating());
    }
}

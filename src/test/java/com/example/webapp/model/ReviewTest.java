package com.example.webapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewTest {

    private Review review;

    @BeforeEach
    void setUp() {
        review = new Review();
        review.setId(1L);
        review.setUsername("testuser");
        review.setReviewText("This is a sample review.");
        review.setRating(5);
        review.setReviewDate(LocalDate.of(2023, 10, 1));
        review.setLikes(10);
        review.setDislikes(2);
    }

    @Test
    void testGetAndSetId() {
        review.setId(123L);
        assertEquals(123L, review.getId());
    }

    @Test
    void testGetAndSetUsername() {
        review.setUsername("newuser");
        assertEquals("newuser", review.getUsername());
    }

    @Test
    void testGetAndSetReviewText() {
        review.setReviewText("Updated review text.");
        assertEquals("Updated review text.", review.getReviewText());
    }

    @Test
    void testGetAndSetRating() {
        review.setRating(4);
        assertEquals(4, review.getRating());
    }

    @Test
    void testGetAndSetReviewDate() {
        LocalDate newDate = LocalDate.of(2022, 8, 20);
        review.setReviewDate(newDate);
        assertEquals(newDate, review.getReviewDate());
    }

    @Test
    void testGetAndSetLikes() {
        review.setLikes(15);
        assertEquals(15, review.getLikes());
    }

    @Test
    void testGetAndSetDislikes() {
        review.setDislikes(3);
        assertEquals(3, review.getDislikes());
    }

    @Test
    void testGetAndSetBook() {
        Book book = new Book();
        book.setId("book123");
        book.setTitle("Sample Book");
        review.setBook(book);
        assertEquals("Sample Book", review.getBook().getTitle());
    }

    @Test
    void testAddLikedByUser() {
        Set<String> likedBy = new HashSet<>();
        likedBy.add("user1");
        likedBy.add("user2");
        review.setLikedBy(likedBy);
        assertEquals(2, review.getLikedBy().size());
        assertTrue(review.getLikedBy().contains("user1"));
        assertTrue(review.getLikedBy().contains("user2"));
    }

    @Test
    void testAddDislikedByUser() {
        Set<String> dislikedBy = new HashSet<>();
        dislikedBy.add("user3");
        dislikedBy.add("user4");
        review.setDislikedBy(dislikedBy);
        assertEquals(2, review.getDislikedBy().size());
        assertTrue(review.getDislikedBy().contains("user3"));
        assertTrue(review.getDislikedBy().contains("user4"));
    }

    @Test
    void testLikeAndDislikeCounts() {
        // Test incrementarea și decrementarea like-urilor și dislike-urilor
        review.setLikes(5);
        review.setDislikes(2);

        review.setLikes(review.getLikes() + 1); // Incrementare like
        review.setDislikes(review.getDislikes() + 1); // Incrementare dislike

        assertEquals(6, review.getLikes());
        assertEquals(3, review.getDislikes());
    }
}

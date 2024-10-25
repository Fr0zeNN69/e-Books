package com.example.webapp.controller;

import com.example.webapp.model.Book;
import com.example.webapp.model.Review;
import com.example.webapp.repository.BookRepository;
import com.example.webapp.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReviewControllerTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Model model;

    @InjectMocks
    private ReviewController reviewController;

    private Book book;
    private Review review;
    private final String bookId = "1";
    private final String username = "testuser";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        book = new Book();
        book.setId(bookId);
        review = new Review();
        review.setBook(book);
        review.setUsername(username);
        review.setReviewText("Great book!");
        review.setRating(5);
        review.setReviewDate(LocalDate.now());
    }

    // Test pentru adăugarea unei recenzii
    @Test
    void testAddReview_Success() {
        when(authentication.getName()).thenReturn(username);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(reviewRepository.findByBookIdAndUsername(bookId, username)).thenReturn(Collections.emptyList());

        String result = reviewController.addReview(bookId, "Nice book", 4, authentication, redirectAttributes);

        verify(reviewRepository).save(any(Review.class));
        verify(redirectAttributes).addFlashAttribute("success", "Recenzia a fost adăugată cu succes.");
        assertEquals("redirect:/reviews/view?bookId=" + bookId, result);
    }

    @Test
    void testAddReview_InvalidRating() {
        when(authentication.getName()).thenReturn(username);

        String result = reviewController.addReview(bookId, "Nice book", 6, authentication, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("error", "Ratingul trebuie să fie între 1 și 5 stele.");
        assertEquals("redirect:/reviews/view?bookId=" + bookId, result);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testAddReview_ExistingReview() {
        when(authentication.getName()).thenReturn(username);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(reviewRepository.findByBookIdAndUsername(bookId, username)).thenReturn(Collections.singletonList(review));

        String result = reviewController.addReview(bookId, "Nice book", 4, authentication, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("error", "Ai deja o recenzie pentru această carte.");
        assertEquals("redirect:/reviews/view?bookId=" + bookId, result);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    // Test pentru like
    @Test
    void testLikeReview_Success() {
        review.setLikes(0);
        review.setDislikes(0);
        review.setLikedBy(new HashSet<>());
        review.setDislikedBy(new HashSet<>());

        when(authentication.getName()).thenReturn(username);
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));

        Map<String, Object> response = reviewController.likeReview(1L, authentication);

        assertEquals(1, review.getLikes());
        assertEquals(0, review.getDislikes());
        assertEquals(1, review.getLikedBy().size());
        assertEquals(username, review.getLikedBy().iterator().next());
        verify(reviewRepository).save(review);
        assertEquals(1, response.get("likes"));
        assertEquals(0, response.get("dislikes"));
    }

    @Test
    void testLikeReview_AlreadyLiked() {
        review.setLikedBy(new HashSet<>(Collections.singletonList(username)));
        when(authentication.getName()).thenReturn(username);
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));

        Map<String, Object> response = reviewController.likeReview(1L, authentication);

        assertEquals("Ai dat deja like.", response.get("error"));
    }

    // Test pentru dislike
    @Test
    void testDislikeReview_Success() {
        review.setLikes(1);
        review.setDislikes(0);
        review.setLikedBy(new HashSet<>(Collections.singleton(username)));
        review.setDislikedBy(new HashSet<>());

        when(authentication.getName()).thenReturn(username);
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));

        Map<String, Object> response = reviewController.dislikeReview(1L, authentication);

        assertEquals(0, review.getLikes());
        assertEquals(1, review.getDislikes());
        assertEquals(1, review.getDislikedBy().size());
        verify(reviewRepository).save(review);
        assertEquals(0, response.get("likes"));
        assertEquals(1, response.get("dislikes"));
    }

    @Test
    void testDislikeReview_AlreadyDisliked() {
        review.setDislikedBy(new HashSet<>(Collections.singletonList(username)));
        when(authentication.getName()).thenReturn(username);
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));

        Map<String, Object> response = reviewController.dislikeReview(1L, authentication);

        assertEquals("Ai dat deja dislike.", response.get("error"));
    }
}

package com.example.webapp.controller;

import com.example.webapp.model.Book;
import com.example.webapp.model.Review;
import com.example.webapp.repository.BookRepository;
import com.example.webapp.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;  // Injectează repository-ul pentru Book

    // Adaugă o nouă recenzie
    @PostMapping("/add")
    public String addReview(@RequestParam("bookId") String bookId,
                            @RequestParam("reviewText") String reviewText,
                            @RequestParam("rating") int rating,
                            Authentication authentication) {
        String username = authentication.getName();

        // Găsește cartea pe baza ID-ului
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + bookId));

        // Creează recenzia
        Review review = new Review();
        review.setBook(book);  // Asociază recenzia cu cartea
        review.setReviewText(reviewText);
        review.setRating(rating);
        review.setUsername(username);
        review.setReviewDate(LocalDate.now());

        // Salvează recenzia în baza de date
        reviewRepository.save(review);

        return "redirect:/reviews/view?bookId=" + bookId;
    }

    // Vizualizează recenziile pentru o anumită carte
    @GetMapping("/view")
    public String viewReviews(@RequestParam("bookId") String bookId, Model model) {
        // Găsește cartea pe baza ID-ului
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + bookId));

        // Găsește recenziile asociate cărții
        List<Review> reviews = book.getReviews();

        // Adaugă informațiile necesare în model
        model.addAttribute("reviews", reviews);
        model.addAttribute("book", book);

        return "reviews";  // Afișează pagina reviews.html
    }
}

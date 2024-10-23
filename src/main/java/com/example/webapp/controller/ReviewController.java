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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;  // Injectează repository-ul pentru Book

    /**
     * Adaugă o nouă recenzie
     */
    @PostMapping("/add")
    public String addReview(@RequestParam("bookId") String bookId,
                            @RequestParam("reviewText") String reviewText,
                            @RequestParam("rating") int rating,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        String username = authentication.getName();

        // Validare rating
        if (rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("error", "Ratingul trebuie să fie între 1 și 5 stele.");
            return "redirect:/reviews/view?bookId=" + bookId;
        }

        // Găsește cartea pe baza ID-ului
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + bookId));

        // Previne adăugarea mai multor recenzii pentru aceeași carte de către același utilizator
        List<Review> existingReviews = reviewRepository.findByBookIdAndUsername(bookId, username);
        if (!existingReviews.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Ai deja o recenzie pentru această carte.");
            return "redirect:/reviews/view?bookId=" + bookId;
        }

        // Creează recenzia
        Review review = new Review();
        review.setBook(book);  // Asociază recenzia cu cartea
        review.setReviewText(reviewText);
        review.setRating(rating);
        review.setUsername(username);
        review.setReviewDate(LocalDate.now());

        // Salvează recenzia în baza de date
        reviewRepository.save(review);

        redirectAttributes.addFlashAttribute("success", "Recenzia a fost adăugată cu succes.");
        return "redirect:/reviews/view?bookId=" + bookId;
    }

    /**
     * Vizualizează recenziile pentru o anumită carte
     */
    @GetMapping("/view")
    public String viewReviews(@RequestParam("bookId") String bookId,
                              Authentication authentication,
                              Model model) {
        String username = authentication.getName();
        model.addAttribute("username", username);

        // Găsește cartea pe baza ID-ului
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + bookId));

        // Găsește recenziile asociate cărții
        List<Review> reviews = reviewRepository.findByBookId(bookId);

        // Calculează ratingul mediu
        double averageRating = 0.0;
        if (!reviews.isEmpty()) {
            averageRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        }

        // Adaugă informațiile necesare în model
        model.addAttribute("reviews", reviews);
        model.addAttribute("book", book);
        model.addAttribute("averageRating", averageRating);

        return "reviews";  // Afișează pagina reviews.html
    }
}

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
import java.util.*;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    // Adaugare recenzie
    @PostMapping("/add")
    public String addReview(@RequestParam("bookId") String bookId,
                            @RequestParam("reviewText") String reviewText,
                            @RequestParam("rating") int rating,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        String username = authentication.getName();

        if (rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("error", "Ratingul trebuie să fie între 1 și 5 stele.");
            return "redirect:/reviews/view?bookId=" + bookId;
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + bookId));

        List<Review> existingReviews = reviewRepository.findByBookIdAndUsername(bookId, username);
        if (!existingReviews.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Ai deja o recenzie pentru această carte.");
            return "redirect:/reviews/view?bookId=" + bookId;
        }

        Review review = new Review();
        review.setBook(book);
        review.setReviewText(reviewText);
        review.setRating(rating);
        review.setUsername(username);
        review.setReviewDate(LocalDate.now());

        reviewRepository.save(review);

        redirectAttributes.addFlashAttribute("success", "Recenzia a fost adăugată cu succes.");
        return "redirect:/reviews/view?bookId=" + bookId;
    }

    // Vizualizarea recenziilor cu sortare
    @GetMapping("/view")
    public String viewReviews(@RequestParam("bookId") String bookId,
                              @RequestParam(value = "sort", defaultValue = "recent") String sort,
                              Authentication authentication,
                              Model model) {
        // Obtinem username ul autentificat sau "Guest" daca utilizatorul nu este autentificat
        String currentUsername = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "Guest";
        model.addAttribute("currentUsername", currentUsername);


        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + bookId));

        List<Review> reviews = reviewRepository.findByBookId(bookId);

        // Sortarea recenziilor in functie de criteriul selectat
        switch (sort) {
            case "stars":
                reviews.sort(Comparator.comparingInt(Review::getRating).reversed());
                break;
            case "likes":
                reviews.sort(Comparator.comparingInt(Review::getLikes).reversed());
                break;
            case "recent":
            default:
                reviews.sort(Comparator.comparing(Review::getReviewDate).reversed());
                break;
        }

        double averageRating = 0.0;
        if (!reviews.isEmpty()) {
            averageRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("book", book);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("sort", sort);  // Trimitem criteriul de sortare catre view

        return "reviews";
    }

    // Metoda pentru like
    @PostMapping("/like/{reviewId}")
    @ResponseBody
    public Map<String, Object> likeReview(@PathVariable Long reviewId, Authentication authentication) {
        String username = authentication.getName();
        Map<String, Object> response = new HashMap<>();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid review Id: " + reviewId));

        // Verifica dacă utilizatorul a dat deja dislike si dorește sa schimbe votul
        if (review.getDislikedBy().contains(username)) {
            review.setDislikes(review.getDislikes() - 1);  // Scade nr de dislike-uri
            review.getDislikedBy().remove(username);       // Scoate utilizatorul din setul de dislikes
        }

        if (review.getLikedBy().contains(username)) {
            response.put("error", "Ai dat deja like.");
        } else {
            review.setLikes(review.getLikes() + 1);       // Creste nr de like-uri
            review.getLikedBy().add(username);            // Adauga utilizatorul in setul de likes
            reviewRepository.save(review);
        }

        response.put("likes", review.getLikes());
        response.put("dislikes", review.getDislikes());
        return response;
    }

    // Metoda pentru dislike
    @PostMapping("/dislike/{reviewId}")
    @ResponseBody
    public Map<String, Object> dislikeReview(@PathVariable Long reviewId, Authentication authentication) {
        String username = authentication.getName();
        Map<String, Object> response = new HashMap<>();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid review Id: " + reviewId));

        // Verifica daca utilizatorul a dat deja like si dorește sa schimbe votul
        if (review.getLikedBy().contains(username)) {
            review.setLikes(review.getLikes() - 1);  // Scade nr de like-uri
            review.getLikedBy().remove(username);    // Scoate utilizatorul din setul de likes
        }

        if (review.getDislikedBy().contains(username)) {
            response.put("error", "Ai dat deja dislike.");
        } else {
            review.setDislikes(review.getDislikes() + 1);  // Creste nr de dislike-uri
            review.getDislikedBy().add(username);          // Adauga utilizatorul in setul de dislikes
            reviewRepository.save(review);
        }

        response.put("likes", review.getLikes());
        response.put("dislikes", review.getDislikes());
        return response;
    }
}

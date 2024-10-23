package com.example.webapp.controller;
import com.example.webapp.model.Book;
import com.example.webapp.model.Review;

import com.example.webapp.model.User;
import com.example.webapp.repository.BookRepository;
import com.example.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@Controller
public class HomeController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        if (authentication != null) {
            String username = authentication.getName();
            model.addAttribute("username", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

            Set<Book> favoriteBooks = user.getFavoriteBooks();

            // Calculează ratingul mediu pentru fiecare carte favorită
            for (Book book : favoriteBooks) {
                double averageRating = book.getReviews().stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);
                book.setAverageRating(averageRating);
            }

            model.addAttribute("books", favoriteBooks);

            if (favoriteBooks.isEmpty()) {
                model.addAttribute("message", "You have no favorite books.");
            }
        } else {
            model.addAttribute("message", "You need to log in to see your favorite books.");
        }

        return "home";
    }
}

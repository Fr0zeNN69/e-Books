package com.example.webapp.controller;

import com.example.webapp.model.Book;
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
            String username = authentication.getName();  // Obține utilizatorul conectat
            model.addAttribute("username", username);  // Adaugă username-ul în model

            // Găsește utilizatorul
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

            // Preia cărțile favorite ale utilizatorului
            Set<Book> favoriteBooks = user.getFavoriteBooks();
            model.addAttribute("books", favoriteBooks);

            // Dacă nu există cărți favorite, adaugă un mesaj
            if (favoriteBooks.isEmpty()) {
                model.addAttribute("message", "You have no favorite books.");
            }
        } else {
            model.addAttribute("message", "You need to log in to see your favorite books.");
        }

        return "home";  // Returnează pagina home.html
    }
}

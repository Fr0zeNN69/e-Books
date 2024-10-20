package com.example.webapp.controller;

import com.example.webapp.model.Book;
import com.example.webapp.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        if (authentication != null) {
            String username = authentication.getName();  // Obține utilizatorul conectat
            model.addAttribute("username", username);  // Adaugă username-ul în model

            // Poți prelua cărți stocate în baza de date
            List<Book> books = bookRepository.findAll();
            model.addAttribute("books", books);  // Adaugă cărțile pentru afișare
        }

        return "home";  // Returnează pagina home.html
    }
}

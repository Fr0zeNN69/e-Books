package com.example.webapp.controller;

import com.example.webapp.model.Book;
import com.example.webapp.model.User;
import com.example.webapp.repository.BookRepository;
import com.example.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BookRepository bookRepository;

    // RESTful methods
    @PostMapping("/add")
    @ResponseBody
    public User addUser(@RequestBody User user) {
        // Criptează parola înainte de a salva utilizatorul
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @GetMapping("/all")
    @ResponseBody
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Metodă pentru înregistrarea utilizatorilor
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register"; // Afișează pagina HTML register.html
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        // Verificare în log dacă metoda este apelată corect
        System.out.println("Utilizatorul se înregistrează: " + user.getUsername());

        // Criptează parola înainte de a salva
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return "redirect:/login";
    }

    // Endpoint pentru adăugarea unei cărți la favorite
    @PostMapping("/favorites/add")
    @ResponseBody
    public ResponseEntity<String> addFavorite(@RequestParam("bookId") String bookId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("Utilizator neautentificat", HttpStatus.UNAUTHORIZED);
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilizator invalid"));

        // Obține cartea
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("ID carte invalid: " + bookId));

        // Adaugă la favorite dacă nu există deja
        if (!user.getFavoriteBooks().contains(book)) {
            user.getFavoriteBooks().add(book);
            userRepository.save(user);
        }

        return new ResponseEntity<>("Cartea a fost adăugată la favorite", HttpStatus.OK);
    }

    // Endpoint pentru eliminarea unei cărți din favorite
    @PostMapping("/favorites/remove")
    @ResponseBody
    public ResponseEntity<String> removeFavorite(@RequestParam("bookId") String bookId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("Utilizator neautentificat", HttpStatus.UNAUTHORIZED);
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilizator invalid"));

        // Obține cartea
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("ID carte invalid: " + bookId));

        // Elimină din favorite dacă există
        if (user.getFavoriteBooks().contains(book)) {
            user.getFavoriteBooks().remove(book);
            userRepository.save(user);
        }

        return new ResponseEntity<>("Cartea a fost eliminată din favorite", HttpStatus.OK);
    }
}

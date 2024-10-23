package com.example.webapp.controller;

import com.example.webapp.model.Book;
import com.example.webapp.model.User;
import com.example.webapp.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import com.example.webapp.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/search")
    public String searchBooks(@RequestParam("query") String query, Model model, Authentication authentication) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + query;

        // Apel API și obține JSON ca String
        String response = restTemplate.getForObject(url, String.class);

        // Parsează răspunsul JSON
        ObjectMapper mapper = new ObjectMapper();
        List<Book> books = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(response);
            JsonNode items = root.path("items");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    Book book = new Book();
                    book.setId(item.path("id").asText());
                    book.setTitle(item.path("volumeInfo").path("title").asText());

                    // Extrage autorii
                    JsonNode authorsNode = item.path("volumeInfo").path("authors");
                    if (authorsNode.isArray()) {
                        List<String> authorsList = new ArrayList<>();
                        for (JsonNode authorNode : authorsNode) {
                            authorsList.add(authorNode.asText());
                        }
                        book.setAuthors(String.join(", ", authorsList));
                    } else {
                        book.setAuthors("Unknown");
                    }

                    // Extrage descrierea cărții
                    String description = item.path("volumeInfo").path("description").asText(null);
                    if (description == null || description.isEmpty()) {
                        description = "Description not available.";
                    }
                    book.setDescription(description);

                    // Verifică dacă cartea există deja în baza de date și salveaz-o dacă nu există
                    if (!bookRepository.existsById(book.getId())) {
                        bookRepository.save(book);
                    }

                    books.add(book);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Adaugă cărțile în model
        model.addAttribute("books", books);

        // Obține lista de bookIds din favorite
        Set<String> favoriteBookIds = new HashSet<>();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                for (Book favBook : user.getFavoriteBooks()) {
                    favoriteBookIds.add(favBook.getId());
                }
            }
        }
        model.addAttribute("favoriteBookIds", favoriteBookIds);

        // Obține numele utilizatorului autentificat și adaugă-l în model
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            model.addAttribute("username", authentication.getName());
        } else {
            model.addAttribute("username", "User");
        }

        return "book_search_results";
    }
}

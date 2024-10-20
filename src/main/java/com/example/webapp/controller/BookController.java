package com.example.webapp.controller;

import com.example.webapp.model.Book;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
// Asigură-te că ai importat BookRepository
import com.example.webapp.repository.BookRepository;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Controller
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/search")
    public String searchBooks(@RequestParam("query") String query, Model model) {
        RestTemplate restTemplate = new RestTemplate();
        // Dacă ai adăugat cheia API în application.properties, poți să o utilizezi aici
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + query;

        // Apel API și obține JSON ca String
        String response = restTemplate.getForObject(url, String.class);

        // Parsează răspunsul JSON
        ObjectMapper mapper = new ObjectMapper();
        List<Book> books = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(response);
            JsonNode items = root.path("items");

            // Iterează prin rezultate și extrage titlul și autorii
            if (items.isArray()) {
                for (JsonNode item : items) {
                    Book book = new Book();

                    // Setează ID-ul cărții folosind ID-ul de la Google Books
                    book.setId(item.path("id").asText());


                    // Setează titlul cărții
                    book.setTitle(item.path("volumeInfo").path("title").asText());

                    // Extrage autorii și setează-i ca un șir de caractere
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

        // Adaugă cărțile parseate în model pentru afișare
        model.addAttribute("books", books);

        return "book_search_results"; // redirecționează la pagina book_search_results.html
    }
}

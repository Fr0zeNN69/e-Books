package com.example.webapp.controller;

import com.example.webapp.model.Book;
import com.example.webapp.model.Review;  // Import corect pentru Review
import com.example.webapp.model.User;
import com.example.webapp.repository.BookRepository;
import com.example.webapp.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${google.books.api.key}")
    private String apiKey;

    @GetMapping("/search")
    public String searchBooks(
            @RequestParam("query") String query,
            Model model,
            Authentication authentication) {

        RestTemplate restTemplate = new RestTemplate();
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + encodedQuery + "&maxResults=40&key=" + apiKey;

        String response = restTemplate.getForObject(url, String.class);
        ObjectMapper mapper = new ObjectMapper();
        List<Book> books = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(response);
            JsonNode items = root.path("items");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    Book book = parseBookFromJson(item);
                    if (book != null) {
                        // Verifică dacă cartea există deja în baza de date
                        Optional<Book> existingBook = bookRepository.findById(book.getId());

                        if (existingBook.isPresent()) {
                            // Dacă cartea există deja, adaugă recenziile locale și calculează ratingul mediu
                            book = existingBook.get();
                            double averageRating = book.getReviews().stream()
                                    .mapToInt(Review::getRating)
                                    .average()
                                    .orElse(0.0);
                            book.setAverageRating(averageRating);
                        } else {
                            // Dacă nu există, salvează cartea în baza de date fără recenzii
                            bookRepository.save(book);
                        }
                        books.add(book);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("books", books);
        Set<String> favoriteBookIds = getFavoriteBookIds(authentication);
        model.addAttribute("favoriteBookIds", favoriteBookIds);
        model.addAttribute("username", getUsername(authentication));

        return "book_search_results";
    }



    /**
     * Metoda pentru căutarea cărților după gen.
     */
    @GetMapping("/searchByGenre")
    public String searchBooksByGenre(
            @RequestParam("genre") String genre,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            Authentication authentication) {

        RestTemplate restTemplate = new RestTemplate();
        String encodedGenre = URLEncoder.encode(genre, StandardCharsets.UTF_8);
        int maxResults = 40; // Maxim 40 pe cerere
        int startIndex = page * maxResults;
        String url = "https://www.googleapis.com/books/v1/volumes?q=subject:" + encodedGenre +
                "&maxResults=" + maxResults + "&startIndex=" + startIndex + "&key=" + apiKey;

        // Apel API și prelucrare răspuns
        String response = restTemplate.getForObject(url, String.class);
        ObjectMapper mapper = new ObjectMapper();
        List<Book> books = new ArrayList<>();
        int totalItems = 0;

        try {
            JsonNode root = mapper.readTree(response);
            totalItems = root.path("totalItems").asInt();
            JsonNode items = root.path("items");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    Book book = parseBookFromJson(item);
                    if (book != null && containsDesiredGenre(book, genre)) {
                        // Verifică dacă cartea există deja în baza de date și salveaz-o dacă nu există
                        if (!bookRepository.existsById(book.getId())) {
                            bookRepository.save(book);
                        }
                        books.add(book);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Adaugă cărțile în model
        model.addAttribute("books", books);

        // Calculă numărul total de pagini
        int totalPages = (int) Math.ceil((double) totalItems / maxResults);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        // Obține lista de bookIds din favorite
        Set<String> favoriteBookIds = getFavoriteBookIds(authentication);
        model.addAttribute("favoriteBookIds", favoriteBookIds);

        // Adaugă genul în model pentru afișare
        model.addAttribute("genre", genre);

        // Adaugă numele utilizatorului autentificat în model
        model.addAttribute("username", getUsername(authentication));

        return "book_search_results";
    }
    @GetMapping("/topBooks")
    public String getTopBooks(Model model, Authentication authentication) {
        // Preluăm toate cărțile din baza de date
        List<Book> books = bookRepository.findAll();

        // Calculează ratingul mediu pentru fiecare carte
        for (Book book : books) {
            double averageRating = book.getReviews().stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            book.setAverageRating(averageRating);
        }

        // Sortează cărțile în funcție de rating descrescător
        books.sort(Comparator.comparingDouble(Book::getAverageRating).reversed());

        // Adaugă lista de cărți sortată în model
        model.addAttribute("books", books);

        // Obține lista de bookIds din favorite
        Set<String> favoriteBookIds = getFavoriteBookIds(authentication);
        model.addAttribute("favoriteBookIds", favoriteBookIds);

        // Adaugă numele utilizatorului autentificat în model
        model.addAttribute("username", getUsername(authentication));

        return "top_books";
    }


    /**
     * Metodă auxiliară pentru parsarea unui obiect Book din JsonNode.
     */
    private Book parseBookFromJson(JsonNode item) {
        if (item == null || item.path("volumeInfo").isMissingNode()) {
            return null;
        }

        Book book = new Book();
        book.setId(item.path("id").asText());

        // Setează titlul
        JsonNode titleNode = item.path("volumeInfo").path("title");
        if (!titleNode.isMissingNode()) {
            book.setTitle(titleNode.asText());
        } else {
            book.setTitle("Titlu necunoscut");
        }

        // Extrage autorii
        JsonNode authorsNode = item.path("volumeInfo").path("authors");
        if (authorsNode.isArray()) {
            List<String> authorsList = new ArrayList<>();
            for (JsonNode authorNode : authorsNode) {
                authorsList.add(authorNode.asText());
            }
            book.setAuthors(String.join(", ", authorsList));
        } else {
            book.setAuthors("Autor necunoscut");
        }

        // Extrage descrierea cărții
        String description = item.path("volumeInfo").path("description").asText(null);
        if (description == null || description.isEmpty()) {
            description = "Descriere indisponibilă.";
        }
        book.setDescription(description);

        // Extrage categoriile (genurile)
        JsonNode categoriesNode = item.path("volumeInfo").path("categories");
        List<String> categoriesList = new ArrayList<>();
        if (categoriesNode.isArray()) {
            for (JsonNode categoryNode : categoriesNode) {
                categoriesList.add(categoryNode.asText());
            }
            book.setCategories(String.join(", ", categoriesList));
        } else {
            book.setCategories("Categorie necunoscută");
        }

        return book;
    }

    /**
     * Metodă auxiliară pentru verificarea dacă un Book conține genul dorit.
     */
    private boolean containsDesiredGenre(Book book, String genre) {
        if (book.getCategories() == null || book.getCategories().isEmpty()) {
            return false;
        }
        String[] genres = book.getCategories().split(",\\s*");
        for (String g : genres) {
            if (g.equalsIgnoreCase(genre)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Metodă auxiliară pentru obținerea setului de bookIds favorite ale utilizatorului.
     */
    private Set<String> getFavoriteBookIds(Authentication authentication) {
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
        return favoriteBookIds;
    }

    /**
     * Metodă auxiliară pentru obținerea numelui utilizatorului autentificat.
     */
    private String getUsername(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            return authentication.getName();
        } else {
            return "User";
        }
    }
}

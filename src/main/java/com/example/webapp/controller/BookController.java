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
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "sort", required = false, defaultValue = "none") String sort,
            @RequestParam(value = "order", required = false, defaultValue = "asc") String order,
            @RequestParam(value = "genre", required = false) String genre,
            Model model,
            Authentication authentication) {

        List<Book> books = new ArrayList<>();
        boolean searchActive = false;

        if (authentication != null) {
            String currentUsername = authentication.getName();
            model.addAttribute("currentUsername", currentUsername);
        } else {
            model.addAttribute("currentUsername", "Guest");
        }

        // Dacă există un query, autor sau gen pentru căutare
        if ((query != null && !query.trim().isEmpty()) ||
                (author != null && !author.trim().isEmpty()) ||
                (genre != null && !genre.trim().isEmpty())) {

            searchActive = true;

            // Construim URL-ul API în funcție de parametri
            RestTemplate restTemplate = new RestTemplate();
            StringBuilder urlBuilder = new StringBuilder("https://www.googleapis.com/books/v1/volumes?q=");

            if (query != null && !query.trim().isEmpty()) {
                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
                urlBuilder.append(encodedQuery);
            }

            if (author != null && !author.trim().isEmpty()) {
                String encodedAuthor = URLEncoder.encode(author, StandardCharsets.UTF_8);
                urlBuilder.append("+inauthor:").append(encodedAuthor);
            }

            if (genre != null && !genre.trim().isEmpty()) {
                String encodedGenre = URLEncoder.encode(genre, StandardCharsets.UTF_8);
                urlBuilder.append("+subject:").append(encodedGenre);
            }

            urlBuilder.append("&maxResults=40&key=").append(apiKey);

            // Apelăm API-ul Google Books
            String response = restTemplate.getForObject(urlBuilder.toString(), String.class);
            ObjectMapper mapper = new ObjectMapper();

            try {
                JsonNode root = mapper.readTree(response);
                JsonNode items = root.path("items");

                if (items.isArray()) {
                    for (JsonNode item : items) {
                        Book book = parseBookFromJson(item);
                        if (book != null) {
                            Optional<Book> existingBook = bookRepository.findById(book.getId());

                            if (existingBook.isPresent()) {
                                book = existingBook.get();
                                double averageRating = book.getReviews().stream()
                                        .mapToInt(Review::getRating)
                                        .average()
                                        .orElse(0.0);
                                book.setAverageRating(averageRating);
                            } else {
                                bookRepository.save(book);
                            }
                            books.add(book);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Sortăm în funcție de parametrii
            if (sort.equals("rating")) {
                books.sort(Comparator.comparingDouble(Book::getAverageRating));
            } else if (sort.equals("author")) {
                books.sort(Comparator.comparing(Book::getAuthors));
            } else if (sort.equals("title")) {
                books.sort(Comparator.comparing(Book::getTitle));
            } else if (sort.equals("category")) {
                books.sort(Comparator.comparing(Book::getCategories));
            }

            // Inversăm ordinea dacă e nevoie
            if (order.equals("desc")) {
                Collections.reverse(books);
            }
        }

        model.addAttribute("books", books);
        model.addAttribute("query", query);
        model.addAttribute("author", author);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);  // Trimitem și ordinea
        model.addAttribute("genre", genre);  // Trimitem și genul
        model.addAttribute("searchActive", searchActive);  // Indicator dacă căutarea este activă
        Set<String> favoriteBookIds = getFavoriteBookIds(authentication);
        model.addAttribute("favoriteBookIds", favoriteBookIds);
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

        // Adaugă numele utilizatorului conectat în model
        if (authentication != null && authentication.isAuthenticated()) {
            String currentUsername = authentication.getName();
            model.addAttribute("currentUsername", currentUsername);
        } else {
            model.addAttribute("currentUsername", "Guest");
        }

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
            book.setTitle("Unknown Title");
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
            book.setAuthors("Unknown Author");
        }

        // Extrage descrierea cărții
        String description = item.path("volumeInfo").path("description").asText(null);
        if (description == null || description.isEmpty()) {
            description = "Description not available.";
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
            book.setCategories("Unknown Category");
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
            return null;  // Returnăm null în loc de "User"
        }
    }

}


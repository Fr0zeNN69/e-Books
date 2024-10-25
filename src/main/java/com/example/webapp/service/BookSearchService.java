package com.example.webapp.service;

import com.example.webapp.model.Book;
import com.example.webapp.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BookSearchService {

    private static final String API_URL = "https://www.googleapis.com/books/v1/volumes";

    @Autowired
    private BookRepository bookRepository;

    @Value("${google.books.api.key}")
    private String apiKey;

    public List<Book> searchBooks(String query) {
        // creez o instan de RestTemplate pentru a face cereri HTTP
        RestTemplate restTemplate = new RestTemplate();

        // construiesc URL-ul cererii API
        URI uri = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("q", query)
                .queryParam("key", apiKey)
                .build()
                .toUri();

        // trimite cererea GET si primeste raspunsul ca un Map
        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);

        // Extrage rezultatele din raspuns si converteste le intr o lista de obiecte Book
        List<Book> books = new ArrayList<>();
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

        if (items != null) {
            for (Map<String, Object> item : items) {
                Map<String, Object> volumeInfo = (Map<String, Object>) item.get("volumeInfo");

                Book book = new Book();

                // Seteaza ID-ul cartii folosind id-ul de la Google Books
                book.setId((String) item.get("id"));

                // Seteaza titlul cartii
                book.setTitle((String) volumeInfo.get("title"));


                // Extrage autorii
                Object authorsObj = volumeInfo.get("authors");
                if (authorsObj != null && authorsObj instanceof List) {
                    List<String> authorsList = (List<String>) authorsObj;
                    String authors = String.join(", ", authorsList);
                    book.setAuthors(authors);
                } else {
                    book.setAuthors("Unknown");
                }

                // Verifica si salveaza cartea in baza de date dacă nu exista deja
                if (!bookRepository.existsById(book.getId())) {
                    bookRepository.save(book);
                }

                books.add(book);
            }
        }

        return books;
    }
}

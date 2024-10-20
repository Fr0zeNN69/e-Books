package com.example.webapp.repository;

import com.example.webapp.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    // Metoda personalizată pentru a căuta cărți după titlu
    List<Book> findByTitleContainingIgnoreCase(String title);
}

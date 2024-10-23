package com.example.webapp.repository;

import com.example.webapp.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Găsește recenziile pe baza ID-ului cărții
    List<Review> findByBookId(String bookId);

    // Găsește recenziile pe baza ID-ului cărții și al username-ului
    List<Review> findByBookIdAndUsername(String bookId, String username);
}

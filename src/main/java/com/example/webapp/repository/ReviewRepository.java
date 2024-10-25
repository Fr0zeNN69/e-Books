package com.example.webapp.repository;

import com.example.webapp.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookId(String bookId);

    List<Review> findByBookIdAndUsername(String bookId, String username);

    List<Review> findByUsername(String username);
}

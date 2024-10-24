package com.example.webapp.controller;

import com.example.webapp.model.Book;
import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.model.Review;
import com.example.webapp.repository.ReviewRepository;
import com.example.webapp.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;



@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private S3Service s3Service;

    // Afișarea profilului pentru un utilizator specific
    @GetMapping("/profile/{username}")
    public String showProfile(@PathVariable String username, Model model, Authentication authentication) {
        User profileUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Afișează numele utilizatorului conectat în navbar
        if (authentication != null) {
            String currentUsername = authentication.getName();
            model.addAttribute("currentUsername", currentUsername); // Utilizatorul conectat
        }

        // Afișează profilul utilizatorului vizualizat
        model.addAttribute("user", profileUser);
        model.addAttribute("isCurrentUser", authentication != null && profileUser.getUsername().equals(authentication.getName()));

        // Adaugă restul datelor (recenzii, favorite, etc.)
        List<Review> userReviews = reviewRepository.findByUsername(username);
        List<Book> favoriteBooks = new ArrayList<>(profileUser.getFavoriteBooks());

        model.addAttribute("userReviews", userReviews);
        model.addAttribute("favoriteBooks", favoriteBooks);

        return "profile";
    }


    @PostMapping("/profile/updateBio")
    public String updateBio(@RequestParam String bio, Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            user.setBio(bio);
            userRepository.save(user);

            model.addAttribute("username", user.getUsername());
            model.addAttribute("success", "Bio updated successfully.");

            return "redirect:/profile/" + username;
        }
        return "redirect:/login";
    }


    @PostMapping("/profile/changePassword")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Verifică dacă parola veche este corectă (folosind PasswordEncoder)
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                model.addAttribute("error", "Old password is incorrect.");
                return "redirect:/profile/" + username;
            }

            // Verifică dacă noua parolă și confirmarea coincid
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "New passwords do not match.");
                return "redirect:/profile/" + username;
            }

            // Salvează noua parolă criptată
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            model.addAttribute("success", "Password updated successfully.");
            return "redirect:/profile/" + username;  // Redirecționează la profil după schimbarea parolei
        }
        return "redirect:/login";  // Redirecționează la login dacă utilizatorul nu este autentificat
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/profile/uploadProfilePicture")
    public String uploadProfilePicture(@RequestParam("file") MultipartFile file, Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            try {
                // Ștergem imaginea de profil veche dacă există
                String oldProfileImageUrl = user.getProfileImageUrl();
                if (oldProfileImageUrl != null && !oldProfileImageUrl.isEmpty()) {
                    s3Service.deleteFile(oldProfileImageUrl);
                }

                // Salvăm fișierul nou în S3 și obținem URL-ul
                String profileImageUrl = s3Service.uploadFile(file.getInputStream(), file.getOriginalFilename());

                // Actualizăm utilizatorul cu URL-ul noii poze de profil
                user.setProfileImageUrl(profileImageUrl);
                userRepository.save(user);

                model.addAttribute("success", "Profile picture updated successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "There was an error uploading your profile picture.");
            }

            return "redirect:/profile/" + username;
        }
        return "redirect:/login";
    }




    @PostMapping("/profile/deleteAccount")
    public String deleteAccount(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Șterge toate recenziile asociate cu utilizatorul
            List<Review> userReviews = reviewRepository.findByUsername(username);
            reviewRepository.deleteAll(userReviews);

            // Actualizează recenziile like-uite/dislike-uite de utilizator
            List<Review> allReviews = reviewRepository.findAll();
            for (Review review : allReviews) {
                boolean updated = false;

                // Scoate utilizatorul din lista celor care au dat like
                if (review.getLikedBy().contains(username)) {
                    review.getLikedBy().remove(username);
                    review.setLikes(review.getLikes() - 1);
                    updated = true;
                }

                // Scoate utilizatorul din lista celor care au dat dislike
                if (review.getDislikedBy().contains(username)) {
                    review.getDislikedBy().remove(username);
                    review.setDislikes(review.getDislikes() - 1);
                    updated = true;
                }

                // Dacă recenzia a fost modificată, salvează actualizările
                if (updated) {
                    reviewRepository.save(review);
                }
            }

            // Șterge utilizatorul
            userRepository.delete(user);

            return "redirect:/login?accountDeleted";  // Redirecționează la login
        }
        return "redirect:/login";
    }



}











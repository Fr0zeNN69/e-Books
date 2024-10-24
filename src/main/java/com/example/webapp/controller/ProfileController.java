package com.example.webapp.controller;

import com.example.webapp.model.Book;
import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.model.Review;
import com.example.webapp.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;



@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

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


    @PostMapping("/profile/updateName")
    public String updateName(@RequestParam String name, Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            user.setName(name);
            userRepository.save(user);

            model.addAttribute("username", user.getUsername());
            model.addAttribute("success", "Name updated successfully.");
        }
        return "redirect:/profile";
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
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/updateProfilePicture")
    public String updateProfilePicture(@RequestParam String profilePictureUrl, Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            user.setProfilePictureUrl(profilePictureUrl);
            userRepository.save(user);

            model.addAttribute("username", user.getUsername());
            model.addAttribute("success", "Profile picture updated successfully.");
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/updateSocialLinks")
    public String updateSocialLinks(@RequestParam Set<String> socialLinks, Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            user.setSocialLinks(socialLinks);
            userRepository.save(user);

            model.addAttribute("username", user.getUsername());
            model.addAttribute("success", "Social links updated successfully.");
        }
        return "redirect:/profile";
    }

    @GetMapping("/profile/activity")
    public String showUserActivity(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            List<Review> userReviews = reviewRepository.findByUsername(username);
            List<Book> favoriteBooks = new ArrayList<>(user.getFavoriteBooks());

            model.addAttribute("user", user);
            model.addAttribute("userReviews", userReviews);
            model.addAttribute("favoriteBooks", favoriteBooks);
            model.addAttribute("username", user.getUsername());
        }
        return "user_activity";
    }

    @PostMapping("/profile/deleteAccount")
    public String deleteAccount(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            userRepository.delete(user);  // Șterge utilizatorul

            return "redirect:/login?accountDeleted";  // Redirecționează la login
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/addFriend")
    public String addFriend(@RequestParam Long friendId, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            User friend = userRepository.findById(friendId)
                    .orElseThrow(() -> new UsernameNotFoundException("Friend not found"));

            user.getFriends().add(friend);
            userRepository.save(user);

            return "redirect:/profile";
        }
        return "redirect:/login";
    }
}

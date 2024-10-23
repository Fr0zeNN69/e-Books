package com.example.webapp.controller;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Afișează pagina de setări a profilului utilizatorului.
     */
    @GetMapping("/profile")
    public String showSettingsPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername()); // Adaugă username în model
        }
        return "profile";
    }

    /**
     * Actualizează numele complet al utilizatorului.
     */
    @PostMapping("/profile/updateName")
    public String updateName(@RequestParam String name, Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            user.setName(name);
            userRepository.save(user);

            model.addAttribute("username", user.getUsername()); // Actualizează username în model
            model.addAttribute("success", "Name updated successfully.");
        }
        return "redirect:/profile";
    }

    /**
     * Schimbă parola utilizatorului.
     */
    @PostMapping("/profile/changePassword")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            Model model) {

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Verifică dacă parola curentă este corectă
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                model.addAttribute("error", "Current password is incorrect.");
                model.addAttribute("username", user.getUsername()); // Adaugă username în model
                return "profile";
            }

            // Verifică dacă parolele noi coincid
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "New passwords do not match.");
                model.addAttribute("username", user.getUsername()); // Adaugă username în model
                return "profile";
            }

            // Actualizează parola
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            model.addAttribute("success", "Password changed successfully.");
            model.addAttribute("username", user.getUsername()); // Adaugă username în model
        }
        return "redirect:/profile";
    }
}

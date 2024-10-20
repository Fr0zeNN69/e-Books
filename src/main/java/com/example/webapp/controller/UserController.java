package com.example.webapp.controller;

import com.example.webapp.model.User;
import org.springframework.ui.Model;
import com.example.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // RESTful methods
    @PostMapping("/add")
    @ResponseBody
    public User addUser(@RequestBody User user) {
        // Cripteaza parola inainte de a salva utilizatorul
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @GetMapping("/all")
    @ResponseBody
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Register method - pt inregistrarea utilizatorilor
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register"; // Afiseaza pagina HTML register.html
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        // Verificare in log dacă metoda este apelata corect
        System.out.println("Userul se înregistrează: " + user.getUsername());

        // Cripteaza parola inainte de a salva
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return "redirect:/login";
    }



}

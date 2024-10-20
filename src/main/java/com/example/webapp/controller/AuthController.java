package com.example.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // metoda pt a afisa formularul de login
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // aceasta va cauta login.html in resources/templates
    }


    @GetMapping("/register")
    public String showRegisterForm() {
        return "register"; // returneaza pagina HTML register.html
    }

}

package com.example.webapp.controller;

import com.example.webapp.model.User;
import com.example.webapp.model.Review;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.repository.ReviewRepository;
import com.example.webapp.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProfileControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private ProfileController profileController;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setBio("Old bio");
        mockUser.setPassword("encodedOldPass");
    }

    @Test
    public void testUpdateBio() {
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        String result = profileController.updateBio("New bio", authentication, model);

        verify(userRepository).save(mockUser);
        assertEquals("New bio", mockUser.getBio());
        assertEquals("redirect:/profile/testuser", result);
    }

    @Test
    public void testChangePasswordIncorrectOldPassword() {
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongOldPass", "encodedOldPass")).thenReturn(false);

        String result = profileController.changePassword("wrongOldPass", "newPass", "newPass", authentication, model);

        verify(userRepository, never()).save(mockUser);
        assertEquals("redirect:/profile/testuser", result);
    }

    @Test
    public void testChangePasswordMismatchNewPassword() {
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);

        String result = profileController.changePassword("oldPass", "newPass", "differentNewPass", authentication, model);

        verify(userRepository, never()).save(mockUser);
        assertEquals("redirect:/profile/testuser", result);
    }

    @Test
    public void testChangePasswordSuccess() {
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        String result = profileController.changePassword("oldPass", "newPass", "newPass", authentication, model);

        verify(userRepository).save(mockUser);
        assertEquals("encodedNewPass", mockUser.getPassword());
        assertEquals("redirect:/profile/testuser", result);
    }


    @Test
    public void testDeleteAccount() {
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        Review review = new Review();
        review.setUsername("testuser");
        List<Review> userReviews = new ArrayList<>();
        userReviews.add(review);
        when(reviewRepository.findByUsername(anyString())).thenReturn(userReviews);

        String result = profileController.deleteAccount(authentication);

        verify(reviewRepository).deleteAll(userReviews);
        verify(userRepository).delete(mockUser);
        assertEquals("redirect:/login?accountDeleted", result);
    }
}

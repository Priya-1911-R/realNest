package com.example.realNest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.realNest.dto.UserRegistrationDto;
import com.example.realNest.model.Role;
import com.example.realNest.model.User;
import com.example.realNest.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "home"; // This looks for home.html in templates root
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // This looks for login.html in templates root
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userRequest", new UserRegistrationDto());
        return "register"; // This looks for register.html in templates root
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userRequest") UserRegistrationDto userRequest,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        
        // Check for validation errors
        if (result.hasErrors()) {
            return "register";
        }

        // Check if passwords match
        if (!userRequest.getPassword().equals(userRequest.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.userRequest", "Passwords do not match");
            return "register";
        }

        // Check if email already exists
        if (userService.emailExists(userRequest.getEmail())) {
            result.rejectValue("email", "error.userRequest", "Email already exists");
            return "register";
        }

        try {
            // Create new user
            User user = new User();
            user.setName(userRequest.getName());
            user.setEmail(userRequest.getEmail());
            user.setPassword(userRequest.getPassword()); // This will be encoded in service
            
            // Set role - default to USER
            if (userRequest.getRole() != null && userRequest.getRole().equals("ADMIN")) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER); // Default to USER
        }

            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login?success";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "register";
        }
    }
}
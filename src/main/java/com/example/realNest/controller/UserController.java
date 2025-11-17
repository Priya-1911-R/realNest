package com.example.realNest.controller;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.realNest.model.User;
import com.example.realNest.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // Use constructor injection instead of @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String showUserDashboard(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Optional<User> user = userService.findByEmail(username);
        
        if (user.isPresent()) {
            User currentUser = user.get();
            model.addAttribute("user", currentUser);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("username", username);
            
            // Calculate property statistics
            if (currentUser.getProperties() != null) {
                long totalProperties = currentUser.getProperties().size();
                long approvedProperties = currentUser.getProperties().stream()
                        .filter(property -> property.isApproved())
                        .count();
                long pendingProperties = totalProperties - approvedProperties;
                
                model.addAttribute("userProperties", currentUser.getProperties());
                model.addAttribute("totalProperties", totalProperties);
                model.addAttribute("approvedProperties", approvedProperties);
                model.addAttribute("pendingProperties", pendingProperties);
            } else {
                // Initialize empty values if no properties
                model.addAttribute("userProperties", java.util.Collections.emptyList());
                model.addAttribute("totalProperties", 0);
                model.addAttribute("approvedProperties", 0);
                model.addAttribute("pendingProperties", 0);
            }
            
            return "user/dashboard";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/profile")
    public String showUserProfile(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Optional<User> user = userService.findByEmail(username);
        
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            model.addAttribute("username", username);
            return "user/profile";
        } else {
            return "redirect:/login";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication authentication,
                              @RequestParam String name,
                              @RequestParam String email,
                              RedirectAttributes redirectAttributes) {
        
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setName(name);
            
            // Only update email if it's not already taken by another user
            if (!user.getEmail().equals(email) && userService.emailExists(email)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email already exists!");
                return "redirect:/user/profile";
            }
            
            user.setEmail(email);
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
        }

        return "redirect:/user/profile";
    }

    @GetMapping("/favorites")
    public String showUserFavorites(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Optional<User> user = userService.findByEmail(username);
        
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            model.addAttribute("username", username);
            // For now, return empty favorites - you can implement this later
            return "user/favorites";
        } else {
            return "redirect:/login";
        }
    }
}
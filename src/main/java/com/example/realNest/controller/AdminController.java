package com.example.realNest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.realNest.model.Property;
import com.example.realNest.model.User;
import com.example.realNest.service.PropertyService;
import com.example.realNest.service.UserService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        List<Property> pendingProperties = propertyService.findUnapprovedProperties();
        List<User> allUsers = userService.findAll();
        
        model.addAttribute("pendingProperties", pendingProperties);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("pendingCount", pendingProperties.size());
        
        return "admin/dashboard";
    }

    @GetMapping("/properties/pending")
    public String showPendingProperties(Model model) {
        List<Property> pendingProperties = propertyService.findUnapprovedProperties();
        model.addAttribute("properties", pendingProperties);
        return "admin/pending-properties";
    }

    @PostMapping("/properties/{id}/approve")
    public String approveProperty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            propertyService.approveProperty(id);
            redirectAttributes.addFlashAttribute("successMessage", "Property approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving property: " + e.getMessage());
        }
        return "redirect:/admin/properties/pending";
    }

    @PostMapping("/properties/{id}/reject")
    public String rejectProperty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            propertyService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Property rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting property: " + e.getMessage());
        }
        return "redirect:/admin/properties/pending";
    }

    @GetMapping("/users")
    public String showAllUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    @GetMapping("/settings")
public String adminSettings(Model model) {
    // Add any settings data you need here
    return "admin/settings";
}
}
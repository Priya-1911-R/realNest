package com.example.realNest.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.realNest.model.Property;
import com.example.realNest.model.User;
import com.example.realNest.service.PropertyService;
import com.example.realNest.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/properties")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private UserService userService;

    @GetMapping
public String propertiesRoot() {
    return "redirect:/properties/list";
}

    @GetMapping("/list")
    public String listProperties(Model model) {
        model.addAttribute("properties", propertyService.findAllApprovedProperties());
        return "properties/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("property", new Property());
        return "properties/add";
    }

    @PostMapping("/add")
    public String addProperty(@Valid @ModelAttribute Property property, 
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "properties/add";
        }

        try {
            User owner = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            property.setOwner(owner);
            property.setApproved(false); // New properties need admin approval
            propertyService.save(property);
            
            redirectAttributes.addFlashAttribute("successMessage", "Property listed successfully! It will be visible after admin approval.");
            return "redirect:/user/dashboard";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding property: " + e.getMessage());
            return "properties/add";
        }
    }

    @GetMapping("/view/{id}")
    public String viewProperty(@PathVariable Long id, Model model) {
        Optional<Property> property = propertyService.findById(id);
        if (property.isPresent() && (property.get().isApproved() || isOwnerOrAdmin(property.get()))) {
            model.addAttribute("property", property.get());
            return "properties/view";
        }
        return "redirect:/properties/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, 
                             @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Property> property = propertyService.findById(id);
        if (property.isPresent() && isOwnerOrAdmin(property.get(), userDetails)) {
            model.addAttribute("property", property.get());
            return "properties/edit";
        }
        return "redirect:/user/dashboard";
    }

    @PostMapping("/edit/{id}")
    public String updateProperty(@PathVariable Long id, 
                               @Valid @ModelAttribute Property property, 
                               BindingResult result,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "properties/edit";
        }

        try {
            Optional<Property> existingProperty = propertyService.findById(id);
            if (existingProperty.isPresent() && isOwnerOrAdmin(existingProperty.get(), userDetails)) {
                property.setId(id);
                property.setOwner(existingProperty.get().getOwner()); // Preserve owner
                property.setApproved(existingProperty.get().isApproved()); // Preserve approval status
                propertyService.save(property);
                
                redirectAttributes.addFlashAttribute("successMessage", "Property updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Property not found or access denied!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating property: " + e.getMessage());
        }
        
        return "redirect:/user/dashboard";
    }

    @GetMapping("/delete/{id}")
    public String deleteProperty(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<Property> property = propertyService.findById(id);
            if (property.isPresent() && isOwnerOrAdmin(property.get(), userDetails)) {
                propertyService.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Property deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Property not found or access denied!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting property: " + e.getMessage());
        }
        
        return "redirect:/user/dashboard";
    }

    @GetMapping("/search")
    public String searchProperties(@RequestParam(required = false) String location,
                                 @RequestParam(required = false) String type,
                                 @RequestParam(required = false) Double minPrice,
                                 @RequestParam(required = false) Double maxPrice,
                                 Model model) {
        model.addAttribute("properties", 
            propertyService.searchProperties(location, type, minPrice, maxPrice));
        return "properties/list";
    }

    // Helper method to check if current user is owner or admin
    private boolean isOwnerOrAdmin(Property property) {
        // This method would be used when we don't have UserDetails but need to check in templates
        return false; // Implementation depends on your security context
    }

    private boolean isOwnerOrAdmin(Property property, UserDetails userDetails) {
        if (userDetails == null) return false;
        
        // Check if user is admin
        boolean isAdmin = userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) return true;
        
        // Check if user is the owner
        Optional<User> currentUser = userService.findByEmail(userDetails.getUsername());
        return currentUser.isPresent() && 
               property.getOwner() != null && 
               property.getOwner().getId().equals(currentUser.get().getId());
    }
}
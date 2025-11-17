package com.example.realNest.controller;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.realNest.model.Property;
import com.example.realNest.model.User;
import com.example.realNest.service.PropertyService;
import com.example.realNest.service.UserService;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PropertyService propertyService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    private Property testProperty;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@example.com");
        testUser.setName("Admin User");
        testUser.setPassword("encodedPassword");

        // Create test property
        testProperty = new Property();
        testProperty.setId(1L);
        testProperty.setTitle("Beautiful Villa");
        testProperty.setDescription("A beautiful villa with pool");
        testProperty.setPrice(new BigDecimal("250000.00"));
        testProperty.setLocation("Bali, Indonesia");
        testProperty.setOwner(testUser);
        testProperty.setApproved(false);
    }

    @Test
    void testShowAdminDashboard_Success() throws Exception {
        // Arrange
        List<Property> pendingProperties = Arrays.asList(testProperty);
        List<User> allUsers = Arrays.asList(testUser);

        when(propertyService.findUnapprovedProperties()).thenReturn(pendingProperties);
        when(userService.findAll()).thenReturn(allUsers);

        // Act & Assert
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("pendingProperties"))
                .andExpect(model().attributeExists("totalUsers"))
                .andExpect(model().attributeExists("pendingCount"))
                .andExpect(model().attribute("totalUsers", 1))
                .andExpect(model().attribute("pendingCount", 1));

        // Verify
        verify(propertyService).findUnapprovedProperties();
        verify(userService).findAll();
    }

    @Test
    void testShowPendingProperties_Success() throws Exception {
        // Arrange
        List<Property> pendingProperties = Arrays.asList(testProperty);
        when(propertyService.findUnapprovedProperties()).thenReturn(pendingProperties);

        // Act & Assert
        mockMvc.perform(get("/admin/properties/pending"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/pending-properties"))
                .andExpect(model().attributeExists("properties"))
                .andExpect(model().attribute("properties", pendingProperties));

        // Verify
        verify(propertyService).findUnapprovedProperties();
    }

    @Test
    void testShowPendingProperties_EmptyList() throws Exception {
        // Arrange
        when(propertyService.findUnapprovedProperties()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/admin/properties/pending"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/pending-properties"))
                .andExpect(model().attributeExists("properties"));

        // Verify
        verify(propertyService).findUnapprovedProperties();
    }

    @Test
    void testApproveProperty_Success() throws Exception {
        // Arrange
        doNothing().when(propertyService).approveProperty(1L);

        // Act & Assert
        mockMvc.perform(post("/admin/properties/1/approve"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/properties/pending"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify
        verify(propertyService).approveProperty(1L);
    }

    @Test
    void testApproveProperty_ServiceThrowsException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Property not found")).when(propertyService).approveProperty(1L);

        // Act & Assert
        mockMvc.perform(post("/admin/properties/1/approve"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/properties/pending"))
                .andExpect(flash().attributeExists("errorMessage"));

        // Verify
        verify(propertyService).approveProperty(1L);
    }

    @Test
    void testRejectProperty_Success() throws Exception {
        // Arrange
        doNothing().when(propertyService).deleteById(1L);

        // Act & Assert
        mockMvc.perform(post("/admin/properties/1/reject"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/properties/pending"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify
        verify(propertyService).deleteById(1L);
    }

    @Test
    void testRejectProperty_ServiceThrowsException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Property not found")).when(propertyService).deleteById(1L);

        // Act & Assert
        mockMvc.perform(post("/admin/properties/1/reject"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/properties/pending"))
                .andExpect(flash().attributeExists("errorMessage"));

        // Verify
        verify(propertyService).deleteById(1L);
    }

    @Test
    void testShowAllUsers_Success() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userService.findAll()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("users", users));

        // Verify
        verify(userService).findAll();
    }

    @Test
    void testShowAllUsers_EmptyList() throws Exception {
        // Arrange
        when(userService.findAll()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"));

        // Verify
        verify(userService).findAll();
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        // Arrange
        doNothing().when(userService).deleteById(1L);

        // Act & Assert
        mockMvc.perform(post("/admin/users/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify
        verify(userService).deleteById(1L);
    }

    @Test
    void testDeleteUser_ServiceThrowsException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("User not found")).when(userService).deleteById(1L);

        // Act & Assert
        mockMvc.perform(post("/admin/users/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("errorMessage"));

        // Verify
        verify(userService).deleteById(1L);
    }
}
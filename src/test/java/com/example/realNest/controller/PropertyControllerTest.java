package com.example.realNest.controller;

import com.example.realNest.model.Property;
import com.example.realNest.model.User;
import com.example.realNest.service.PropertyService;
import com.example.realNest.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PropertyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PropertyService propertyService;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private PropertyController propertyController;

    private Property testProperty;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(propertyController).build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testProperty = new Property();
        testProperty.setId(1L);
        testProperty.setTitle("Test Property");
        testProperty.setDescription("Test Description");
        testProperty.setPrice(new BigDecimal("250000"));
        testProperty.setLocation("Test Location");
        testProperty.setOwner(testUser);

        // Mock security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testListProperties() throws Exception {
        when(propertyService.findAllApprovedProperties()).thenReturn(Arrays.asList(testProperty));

        mockMvc.perform(get("/properties"))
                .andExpect(status().isOk())
                .andExpect(view().name("properties/list"))
                .andExpect(model().attributeExists("properties"));

        verify(propertyService).findAllApprovedProperties();
    }

    @Test
    void testShowAddPropertyForm() throws Exception {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/properties/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("properties/add"))
                .andExpect(model().attributeExists("property"));
    }

    @Test
    void testAddProperty_Success() throws Exception {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(propertyService.save(any(Property.class))).thenReturn(testProperty);

        mockMvc.perform(post("/properties/add")
                .param("title", "New Property")
                .param("description", "New Description")
                .param("price", "300000")
                .param("location", "New Location")
                .param("type", "SALE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"));

        verify(propertyService).save(any(Property.class));
    }

    @Test
    void testAddProperty_WithValidationErrors() throws Exception {
        mockMvc.perform(post("/properties/add")
                .param("title", "") // Empty title should cause validation error
                .param("description", "Desc")
                .param("price", "300000")
                .param("location", "Location"))
                .andExpect(status().isOk())
                .andExpect(view().name("properties/add"));

        verify(propertyService, never()).save(any(Property.class));
    }

    @Test
    void testViewProperty_Success() throws Exception {
        when(propertyService.findById(1L)).thenReturn(Optional.of(testProperty));

        mockMvc.perform(get("/properties/view/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("properties/view"))
                .andExpect(model().attributeExists("property"));

        verify(propertyService).findById(1L);
    }

    @Test
    void testViewProperty_NotFound() throws Exception {
        when(propertyService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/properties/view/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties"));

        verify(propertyService).findById(1L);
    }

    @Test
    void testShowEditForm_Success() throws Exception {
        when(propertyService.findById(1L)).thenReturn(Optional.of(testProperty));

        mockMvc.perform(get("/properties/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("properties/edit"))
                .andExpect(model().attributeExists("property"));

        verify(propertyService).findById(1L);
    }

    @Test
    void testUpdateProperty_Success() throws Exception {
        when(propertyService.save(any(Property.class))).thenReturn(testProperty);

        mockMvc.perform(post("/properties/update/1")
                .param("title", "Updated Property")
                .param("description", "Updated Description")
                .param("price", "350000")
                .param("location", "Updated Location")
                .param("type", "SALE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"));

        verify(propertyService).save(any(Property.class));
    }

    @Test
    void testDeleteProperty() throws Exception {
        doNothing().when(propertyService).deleteById(1L);

        mockMvc.perform(get("/properties/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"));

        verify(propertyService).deleteById(1L);
    }

    @Test
    void testSearchProperties() throws Exception {
        when(propertyService.searchProperties("test", "SALE", 200000.0, 300000.0))
                .thenReturn(Arrays.asList(testProperty));

        mockMvc.perform(get("/properties/search")
                .param("location", "test")
                .param("type", "SALE")
                .param("minPrice", "200000")
                .param("maxPrice", "300000"))
                .andExpect(status().isOk())
                .andExpect(view().name("properties/list"))
                .andExpect(model().attributeExists("properties"));

        verify(propertyService).searchProperties("test", "SALE", 200000.0, 300000.0);
    }
}
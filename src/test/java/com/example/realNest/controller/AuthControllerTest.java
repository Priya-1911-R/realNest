package com.example.realNest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.realNest.model.User;
import com.example.realNest.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testShowLoginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testShowRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userRequest"));
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        when(userService.emailExists(anyString())).thenReturn(false);
        when(userService.save(any(User.class))).thenReturn(new User());

        mockMvc.perform(post("/register")
                .param("name", "Test User")
                .param("email", "test@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?success"));

        verify(userService).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() throws Exception {
        when(userService.emailExists(anyString())).thenReturn(true);

        mockMvc.perform(post("/register")
                .param("name", "Test User")
                .param("email", "existing@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("userRequest", "email"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_PasswordMismatch() throws Exception {
        mockMvc.perform(post("/register")
                .param("name", "Test User")
                .param("email", "test@example.com")
                .param("password", "password123")
                .param("confirmPassword", "differentpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasErrors("userRequest"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_WithValidationErrors() throws Exception {
        mockMvc.perform(post("/register")
                .param("name", "") // Empty name
                .param("email", "invalid-email") // Invalid email
                .param("password", "short") // Short password
                .param("confirmPassword", "different")) // Mismatch
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));

        verify(userService, never()).save(any(User.class));
    }
}
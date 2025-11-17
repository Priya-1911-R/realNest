package com.example.realNest.service;

import java.util.List;
import java.util.Optional;

import com.example.realNest.model.User;

public interface UserService {
    
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    User save(User user);
    void deleteById(Long id);
    boolean emailExists(String email);
    User getCurrentUser();
    

    
}
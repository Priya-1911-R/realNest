package com.example.realNest.service;

import java.util.List;
import java.util.Optional;

import com.example.realNest.model.Property;
import com.example.realNest.model.User;

public interface PropertyService {
    List<Property> findAllApprovedProperties();
    Optional<Property> findById(Long id);
    List<Property> findByOwner(User owner);
    Property save(Property property);
    void deleteById(Long id);
    List<Property> searchProperties(String location, String type, Double minPrice, Double maxPrice);
    List<Property> findUnapprovedProperties();
    void approveProperty(Long id);
}
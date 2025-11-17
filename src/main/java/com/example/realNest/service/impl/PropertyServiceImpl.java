package com.example.realNest.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.realNest.model.Property;
import com.example.realNest.model.User;
import com.example.realNest.repository.PropertyRepository;
import com.example.realNest.service.PropertyService;

@Service
public class PropertyServiceImpl implements PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Override
    public List<Property> findAllApprovedProperties() {
        return propertyRepository.findByApprovedTrue();
    }

    @Override
    public Optional<Property> findById(Long id) {
        return propertyRepository.findById(id);
    }

    @Override
    public List<Property> findByOwner(User owner) {
        return propertyRepository.findByOwner(owner);
    }

    @Override
    public Property save(Property property) {
        return propertyRepository.save(property);
    }

    @Override
    public void deleteById(Long id) {
        propertyRepository.deleteById(id);
    }

    @Override
    public List<Property> searchProperties(String location, String type, Double minPrice, Double maxPrice) {
        // Implementation for search functionality
        if (location != null && !location.isEmpty()) {
            return propertyRepository.findByLocationContainingIgnoreCaseAndApprovedTrue(location);
        }
        return findAllApprovedProperties();
    }

    @Override
    public List<Property> findUnapprovedProperties() {
        return propertyRepository.findByApprovedFalse();
    }

    @Override
    public void approveProperty(Long id) {
        Property property = propertyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Property not found"));
        property.setApproved(true);
        propertyRepository.save(property);
    }
}
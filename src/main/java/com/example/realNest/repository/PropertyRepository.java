package com.example.realNest.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.realNest.model.Property;
import com.example.realNest.model.User;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByOwner(User owner);
    List<Property> findByApprovedTrue();
    List<Property> findByLocationContainingIgnoreCaseAndApprovedTrue(String location);
    List<Property> findByTypeAndApprovedTrue(Property.PropertyType type);
    
    @Query("SELECT p FROM Property p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.approved = true")
    List<Property> findByPriceBetweenAndApprovedTrue(@Param("minPrice") BigDecimal minPrice, 
                                                   @Param("maxPrice") BigDecimal maxPrice);
    
    List<Property> findByApprovedFalse();
}
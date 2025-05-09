package com.pupperfield.backend.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pupperfield.backend.entity.Dog;

@Repository
public interface DogRepository extends JpaRepository<Dog, String> {
    @Query("SELECT DISTINCT breed FROM Dog ORDER BY breed ASC")
    Collection<String> getBreeds();
}

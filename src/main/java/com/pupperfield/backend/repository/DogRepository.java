package com.pupperfield.backend.repository;

import com.pupperfield.backend.entity.Dog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface DogRepository extends JpaRepository<Dog, String>, JpaSpecificationExecutor<Dog> {
    @NonNull
    Page<Dog> findAll(Specification<Dog> spec, @NonNull Pageable pageable);

    @Query("SELECT DISTINCT breed FROM Dog ORDER BY breed")
    Collection<String> getBreeds();
}

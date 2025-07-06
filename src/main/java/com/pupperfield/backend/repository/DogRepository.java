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

/**
 * An interface for accessing entities in the database.
 */
@Repository
public interface DogRepository extends JpaRepository<Dog, String>, JpaSpecificationExecutor<Dog> {
    /**
     * Queries the database to find dogs that match the specification.
     *
     * @param spec an object defining the filtering criteria
     * @param pageable an object for pagination and sorting
     * @return an object containing result of the query
     */
    @NonNull
    Page<Dog> findAll(Specification<Dog> spec, @NonNull Pageable pageable);

    /**
     * Retrieves all dog breeds in the database and sorts them alphabetically.
     *
     * @return a data structure containing all dog breeds
     */
    @Query("SELECT DISTINCT breed FROM Dog ORDER BY breed")
    Collection<String> getBreeds();
}

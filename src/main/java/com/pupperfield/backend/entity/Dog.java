package com.pupperfield.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An entity maps to the {@code Dog} table in the database.
 */
@AllArgsConstructor
@Builder
@Entity
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Setter
@Table(name = "Dog")
public class Dog {
    /**
     * Age of the dog.
     */
    @Column(nullable = false, updatable = false)
    @PositiveOrZero
    private long age;

    /**
     * Breed of the dog.
     */
    @Column(nullable = false, updatable = false)
    @NotBlank
    private String breed;

    /**
     * Dog ID or the primary key in the database.
     */
    @Id
    private String id;

    /**
     * Link to an image of the dog.
     */
    @Column(name = "image_link", nullable = false, updatable = false)
    @NotBlank
    private String imageLink;

    /**
     * Name of the dog.
     */
    @Column(nullable = false, updatable = false)
    @NotBlank
    private String name;

    /**
     * Zip code of the dog's location.
     */
    @Column(name = "zip_code", nullable = false, updatable = false)
    @NotBlank
    private String zipCode;
}

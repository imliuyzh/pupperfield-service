package com.pupperfield.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@Entity
@NoArgsConstructor
@Table(name = "Dog")
public class Dog {
    @Column(nullable = false, updatable = false)
    @PositiveOrZero
    private int age;

    @Column(nullable = false, updatable = false)
    @NotBlank
    private String breed;

    @Id
    private String id;
    
    @Column(name = "image_link", nullable = false, updatable = false)
    @NotBlank
    private String img;
    
    @Column(nullable = false, updatable = false)
    @NotBlank
    private String name;

    @Column(name = "zip_code", nullable = false, updatable = false)
    @NotBlank
    private String zipCode;
}

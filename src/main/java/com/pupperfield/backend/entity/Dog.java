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

@AllArgsConstructor
@Builder
@Entity
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Setter
@Table(name = "Dog")
public class Dog {
    @Column(nullable = false, updatable = false)
    @PositiveOrZero
    private long age;

    @Column(nullable = false, updatable = false)
    @NotBlank
    private String breed;

    @Id
    private String id;

    @Column(name = "image_link", nullable = false, updatable = false)
    @NotBlank
    private String imageLink;

    @Column(nullable = false, updatable = false)
    @NotBlank
    private String name;

    @Column(name = "zip_code", nullable = false, updatable = false)
    @NotBlank
    private String zipCode;
}

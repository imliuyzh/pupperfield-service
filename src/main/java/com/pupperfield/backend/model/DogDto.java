package com.pupperfield.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class DogDto {
    private int age;
    private String breed, id, img, name;

    @JsonProperty("zip_code")
    private String zipCode;
}

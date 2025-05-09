package com.pupperfield.backend.mapper;

import org.mapstruct.Mapper;

import com.pupperfield.backend.entity.Dog;
import com.pupperfield.backend.model.DogDto;

@Mapper
public interface DogMapper {
    DogDto dogToDogDto(Dog dog);
}

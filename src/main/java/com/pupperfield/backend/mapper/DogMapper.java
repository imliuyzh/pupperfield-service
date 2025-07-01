package com.pupperfield.backend.mapper;

import com.pupperfield.backend.entity.Dog;
import com.pupperfield.backend.model.DogDto;
import org.mapstruct.Mapper;

@Mapper
public interface DogMapper {
    DogDto dogToDogDto(Dog dog);
}

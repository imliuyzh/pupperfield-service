package com.pupperfield.backend.mapper;

import com.pupperfield.backend.entity.Dog;
import com.pupperfield.backend.model.DogDto;
import org.mapstruct.Mapper;

/**
 * An interface for converting a {@link com.pupperfield.backend.entity.Dog Dog} entity to
 * {@link com.pupperfield.backend.model.DogDto DogDto} data transfer objects. MapStruct will
 * automatically generate the implementation at build time.
 */
@Mapper
public interface DogMapper {
    /**
     * Converts a {@link com.pupperfield.backend.entity.Dog Dog} entity to a
     * {@link com.pupperfield.backend.model.DogDto DogDto}.
     *
     * @param dog a {@link com.pupperfield.backend.entity.Dog Dog} entity
     * @return a {@link com.pupperfield.backend.model.DogDto DogDto} with the same data
     */
    DogDto dogToDogDto(Dog dog);
}

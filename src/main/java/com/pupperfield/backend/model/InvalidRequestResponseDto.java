package com.pupperfield.backend.model;

import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class InvalidRequestResponseDto {
    private String error;
    private Collection<String> detail;
}

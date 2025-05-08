package com.pupperfield.backend.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class InvalidRequestResponseDto {
    private String error;
    private List<String> detail;
}

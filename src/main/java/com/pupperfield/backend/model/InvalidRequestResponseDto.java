package com.pupperfield.backend.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class InvalidRequestResponseDto {
    private String reason;
    private List<String> detail;
}

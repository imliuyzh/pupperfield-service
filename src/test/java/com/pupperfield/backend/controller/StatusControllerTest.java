package com.pupperfield.backend.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class StatusControllerTest {
    @Spy
    private StatusController statusController;

    @Test
    public void testStatusReport() {
        var result = statusController.report();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(HttpStatus.OK.getReasonPhrase(), result.getBody());
    }
}

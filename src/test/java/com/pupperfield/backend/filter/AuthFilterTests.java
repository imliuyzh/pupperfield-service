package com.pupperfield.backend.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
public class AuthFilterTests {
    @InjectMocks
    private AuthFilter authFilter;

    @Spy
    private FilterChain chain;

    @Spy
    private HttpServletRequest request;
    
    @Spy
    private HttpServletResponse response;

    @Test
    public void testUnableToGetWriter() {
        try {
            given(request.getMethod()).willReturn("GET");
            given(request.getRequestURI()).willReturn("/test");
            given(response.getWriter()).willAnswer(invocation -> {
                throw new IOException();
            });

            assertThrows(IOException.class, () -> {
                authFilter.doFilter(request, response, chain);
            });

            verify(request, times(1)).getMethod();
            verify(request, times(4)).getRequestURI();
            verify(response, times(1)).getWriter();
        } catch (Exception exception) {
            fail("No exception should not be caught");
        }
    }
}

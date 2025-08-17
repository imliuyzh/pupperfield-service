package com.pupperfield.backend.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pupperfield.backend.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static com.pupperfield.backend.constant.AuthConstants.COOKIE_NAME;
import static com.pupperfield.backend.constant.AuthConstants.LOGIN_PATH;
import static com.pupperfield.backend.constant.AuthConstants.LOGOUT_PATH;
import static com.pupperfield.backend.constant.DogConstants.DOG_BREEDS_PATH;
import static com.pupperfield.backend.constant.DogConstants.DOG_MATCH_PATH;
import static com.pupperfield.backend.constant.DogConstants.DOGS_PATH;
import static com.pupperfield.backend.constant.DogConstants.DOG_SEARCH_PATH;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthFilterTests {
    @InjectMocks
    private AuthFilter authFilter;

    @Spy
    private FilterChain chain;

    @Spy
    private ObjectMapper objectMapper;

    @Spy
    private HttpServletRequest request;

    @Spy
    private HttpServletResponse response;

    @Spy
    private TokenService tokenService;

    @Test
    public void testFilterPassedThrough() throws Exception {
        given(request.getCookies()).willReturn(new Cookie[] {
            new Cookie(COOKIE_NAME, "vwevwve")
        });
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getRequestURI()).willReturn(DOG_BREEDS_PATH);
        given(tokenService.isValid(any(String.class))).willReturn(true);

        authFilter.doFilter(request, response, chain);

        verify(request, atLeastOnce()).getCookies();
        verify(request, atLeastOnce()).getMethod();
        verify(request, atLeastOnce()).getRequestURI();
        verify(tokenService, times(1)).isValid(any(String.class));
    }

    @Test
    public void testInvalidCookie() throws Exception {
        given(request.getCookies()).willReturn(new Cookie[] {
            new Cookie("cookie-name", "cookie-value")
        });
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getRequestURI()).willReturn(DOG_MATCH_PATH);
        given(response.getWriter()).willReturn(new PrintWriter(new StringWriter()));

        authFilter.doFilter(request, response, chain);

        verify(request, atLeastOnce()).getCookies();
        verify(request, atLeastOnce()).getMethod();
        verify(request, atLeastOnce()).getRequestURI();
        verify(response, atLeastOnce()).getWriter();
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testInvalidToken1() throws Exception {
        given(request.getCookies()).willReturn(null);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getRequestURI()).willReturn(LOGOUT_PATH);
        given(response.getWriter()).willReturn(new PrintWriter(new StringWriter()));

        authFilter.doFilter(request, response, chain);

        verify(request, atLeastOnce()).getCookies();
        verify(request, atLeastOnce()).getMethod();
        verify(request, atLeastOnce()).getRequestURI();
        verify(response, atLeastOnce()).getWriter();
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid_token", "wev3 vq 22b", "", "        ", ",,*(%@())"})
    public void testInvalidToken2(String token) throws Exception {
        given(request.getCookies()).willReturn(new Cookie[] {
            new Cookie(COOKIE_NAME, token)
        });
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getRequestURI()).willReturn(DOGS_PATH);
        given(response.getWriter()).willReturn(new PrintWriter(new StringWriter()));

        authFilter.doFilter(request, response, chain);

        verify(request, atLeastOnce()).getCookies();
        verify(request, atLeastOnce()).getMethod();
        verify(request, atLeastOnce()).getRequestURI();
        verify(tokenService, atLeastOnce()).isValid(any(String.class));
        verify(response, atLeastOnce()).getWriter();
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testShouldNotFilter1() throws Exception {
        given(request.getMethod()).willReturn(HttpMethod.OPTIONS.name());
        authFilter.doFilter(request, response, chain);
        verify(request, atLeastOnce()).getMethod();
    }

    @FieldSource("com.pupperfield.backend.constant.AuthConstants#ALLOWLIST")
    @ParameterizedTest
    public void testShouldNotFilter2(String path) throws Exception {
        given(request.getMethod()).willReturn(
            path.equals(LOGIN_PATH) ? HttpMethod.POST.name() : HttpMethod.GET.name());
        given(request.getRequestURI()).willReturn(path);
        authFilter.doFilter(request, response, chain);
        verify(request, atLeastOnce()).getMethod();
        verify(request, atLeastOnce()).getRequestURI();
    }

    @Test
    public void testUnableToGetWriter() throws Exception {
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getRequestURI()).willReturn(DOG_SEARCH_PATH);
        given(response.getWriter()).willAnswer(invocation -> {
            throw new IOException();
        });

        assertThrows(IOException.class, () -> authFilter.doFilter(request, response, chain));

        verify(request, atLeastOnce()).getMethod();
        verify(request, atLeastOnce()).getRequestURI();
        verify(response, atLeastOnce()).getWriter();
    }
}

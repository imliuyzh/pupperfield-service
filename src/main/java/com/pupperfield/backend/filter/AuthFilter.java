package com.pupperfield.backend.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pupperfield.backend.model.InvalidRequestResponseDto;
import com.pupperfield.backend.service.TokenService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.pupperfield.backend.controller.AuthController.COOKIE_NAME;

/**
 * An authentication filter that validates access token in the cookie. Note requests to some
 * whitelisted endpoints bypass this filter.
 */
@AllArgsConstructor
@Component
@Slf4j
public class AuthFilter extends OncePerRequestFilter {
    private ObjectMapper objectMapper;
    private TokenService tokenService;

    private static final String EXCEPTION_MESSAGE_PREFIX = "Unauthorized due to";
    private static final List<String> WHITELIST = List.of(
        "/api-docs", "/auth/login", "/status", "/swagger-ui"
    );

    /**
     * If the request is not whitelisted, attempts to validate access token from the cookie.
     * If token is invalid or missing, returns an HTTP 401 response.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param chain the filter chain
     * @throws IOException if an input or output exception occurs
     * @throws ServletException if the request cannot be handled
     */
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain chain
    ) throws IOException, ServletException {
        try {
            var cookies = (request.getCookies() != null) ? request.getCookies() : new Cookie[0];
            var accessCookie = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(COOKIE_NAME))
                .findFirst()
                .orElseThrow(() -> new AuthException(
                    "%s missing cookie".formatted(EXCEPTION_MESSAGE_PREFIX)));
            if (tokenService.isValid(accessCookie.getValue()) == false) {
                throw new AuthException("%s invalid token".formatted(EXCEPTION_MESSAGE_PREFIX));
            }
            chain.doFilter(request, response);
        } catch (AuthException exception) {
            log.info(ExceptionUtils.getStackTrace(exception));
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            var printWriter = response.getWriter();
            printWriter.write(objectMapper.writeValueAsString(new InvalidRequestResponseDto(
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                List.of(exception.getMessage().substring(EXCEPTION_MESSAGE_PREFIX.length() + 1))
            )));
            printWriter.close();
        }
    }

    /**
     * Allow requests to bypass this filter if they are OPTIONS requests; or, they are directed to
     * one of the whitelisted endpoints.
     *
     * @param request the HTTP request
     * @return whether the request should bypass filtering
     */
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod())
            || WHITELIST.stream().anyMatch(path -> request.getRequestURI().startsWith(path));
    }
}

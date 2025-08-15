package com.pupperfield.backend.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pupperfield.backend.model.InvalidRequestResponseDto;
import com.pupperfield.backend.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.CredentialNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import static com.pupperfield.backend.controller.AuthController.COOKIE_NAME;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * An authentication filter that validates access token in the cookie. Note requests to some
 * whitelisted endpoints bypass this filter.
 */
@AllArgsConstructor
@Component
@Slf4j
public class AuthFilter extends OncePerRequestFilter {
    private static final String EXCEPTION_MESSAGE_PREFIX = "Unauthorized:";
    private static final List<String> WHITELIST = List.of(
        "/api-docs", "/auth/login", "/status", "/swagger-ui"
    );

    private ObjectMapper objectMapper;
    private TokenService tokenService;

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
                .orElseThrow(() -> new CredentialNotFoundException(
                    "%s missing cookie".formatted(EXCEPTION_MESSAGE_PREFIX)));
            if (tokenService.isValid(accessCookie.getValue()) == false) {
                throw new CredentialException(
                    "%s invalid token".formatted(EXCEPTION_MESSAGE_PREFIX));
            }
            chain.doFilter(request, response);
        } catch (CredentialException exception) {
            log.info(ExceptionUtils.getStackTrace(exception));
            handleUnauthorizedRequest(
                response, exception.getMessage(), request.getHeader(HttpHeaders.ORIGIN));
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
        return OPTIONS.matches(request.getMethod())
            || WHITELIST.stream().anyMatch(path -> request.getRequestURI().startsWith(path));
    }

    /**
     * Write an unauthorized HTTP 401 response with a JSON body that contains the error message.
     *
     * @param response the HTTP response
     * @param message the error message
     * @param origin value of the origin header in the request
     * @throws IOException if an input or output exception occurs
     */
    private void handleUnauthorizedRequest(
        HttpServletResponse response,
        String message,
        String origin
    ) throws IOException {
        try (PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(new InvalidRequestResponseDto(
                UNAUTHORIZED.getReasonPhrase(),
                List.of(message.substring(EXCEPTION_MESSAGE_PREFIX.length() + 1))
            )));

            response.setContentType(APPLICATION_JSON_VALUE);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            response.setStatus(UNAUTHORIZED.value());
        } catch (IOException exception) {
            log.error("Error writing unauthorized response: ", exception);
            throw exception;
        }
    }
}

package com.pupperfield.backend.interceptor;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.pupperfield.backend.controller.AuthenticationController;
import com.pupperfield.backend.service.TokenService;

import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Component
@Order(1)
@Slf4j
public class AuthorizationInterceptor implements HandlerInterceptor {
    private TokenService tokenService;

    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler
    ) throws AuthException, IOException {
        if (request.getMethod().equals(HttpMethod.OPTIONS.name()) == false) {
            if (request.getCookies() == null) {
                throw new AuthException(String.format(
                    "Unauthorized due to missing cookie: %s %s",
                    request.getMethod(), request.getRequestURL().toString()
                ));
            }

            String token = null;
            for (var cookie : request.getCookies()) {
                if (cookie.getName()
                        .equals(AuthenticationController.COOKIE_NAME)) {
                    token = cookie.getValue();
                    break;
                }
            }
            if (token == null || tokenService.isValid(token) == false) {
                throw new AuthException(String.format(
                    "Unauthorized due to invalid token: %s %s",
                    request.getMethod(), request.getRequestURL().toString()
                ));
            }
        }
        return true;
    }
}

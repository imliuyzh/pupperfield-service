package com.pupperfield.backend.interceptor;

import com.pupperfield.backend.service.TokenService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.stream.Stream;

import static com.pupperfield.backend.controller.AuthController.COOKIE_NAME;

@AllArgsConstructor
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private TokenService tokenService;

    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler
    ) throws AuthException {
        if (request.getMethod().equals(HttpMethod.OPTIONS.name()) == false) {
            var cookies = request.getCookies();
            if (cookies == null) {
                throw new AuthException(String.format(
                    "Unauthorized due to missing cookie: %s %s",
                    request.getMethod(), request.getRequestURL().toString()
                ));
            }
            Stream.of(cookies)
                .filter(cookie -> cookie.getName().equals(COOKIE_NAME)
                    && tokenService.isValid(cookie.getValue()))
                .findFirst()
                .orElseThrow(() -> new AuthException(String.format(
                    "Unauthorized due to invalid token: %s %s",
                    request.getMethod(), request.getRequestURL().toString()
                )));
        }
        return true;
    }
}

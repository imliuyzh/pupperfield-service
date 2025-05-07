package com.pupperfield.backend.interceptor;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.pupperfield.backend.controller.AuthenticationController;
import com.pupperfield.backend.service.TokenService;

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
    ) throws IOException {
        if (request.getMethod().equals(HttpMethod.OPTIONS.name()) == false) {
            if (request.getCookies() == null) {
                fail(request, response);
                return false;
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
                fail(request, response);
                return false;
            }
        }
        return true;
    }

    private void fail(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response
    ) throws IOException {
        log.info(String.format(
            "Unauthorized due to missing token: %s %s",
            request.getMethod(), request.getRequestURL().toString()
        ));
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        PrintWriter writer = response.getWriter();
        writer.write("Unauthorized");
        writer.flush();
    }
}

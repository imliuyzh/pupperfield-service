package com.pupperfield.backend.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pupperfield.backend.controller.AuthController;
import com.pupperfield.backend.model.LoginRequestDto;
import lombok.NoArgsConstructor;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AuthRequestBuilder {
    public static MockHttpServletRequestBuilder buildLoginRequest(String email, String name)
        throws JsonProcessingException {
        return post(AuthController.LOGIN_PATH)
            .contentType("application/json")
            .content(new ObjectMapper().writeValueAsString(new LoginRequestDto(email, name)));
    }
}

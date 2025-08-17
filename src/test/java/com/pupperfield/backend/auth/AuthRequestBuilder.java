package com.pupperfield.backend.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pupperfield.backend.model.LoginRequestDto;
import jakarta.servlet.http.Cookie;
import lombok.NoArgsConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.pupperfield.backend.constant.AuthConstants.LOGIN_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AuthRequestBuilder {
    public static MockHttpServletRequestBuilder buildLoginRequest(String email, String name)
        throws JsonProcessingException {
        return post(LOGIN_PATH)
            .contentType("application/json")
            .content(new ObjectMapper().writeValueAsString(new LoginRequestDto(email, name)));
    }

    public static Cookie[] getAuthCookie(MockMvc mockMvc, String email, String name)
        throws Exception {
        return mockMvc.perform(buildLoginRequest(email, name))
            .andReturn()
            .getResponse()
            .getCookies();
    }
}

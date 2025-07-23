package com.pupperfield.backend.controller;

import com.pupperfield.backend.filter.AuthFilter;
import com.pupperfield.backend.service.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static com.pupperfield.backend.auth.AuthRequestBuilder.buildLoginRequest;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({AuthFilter.class, TokenService.class})
@WebMvcTest(AuthController.class)
public class AuthControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testLogIn() throws Exception {
        mockMvc.perform(buildLoginRequest("name@email.com", "name"))
            .andExpect(content().string(HttpStatus.OK.getReasonPhrase()))
            .andExpect(header().string("Set-Cookie", matchesPattern("fetch-access-token=\\S+;.*")))
            .andExpect(status().isOk());
    }

    @Test
    public void testLogInWithSubdomain() throws Exception {
        mockMvc.perform(buildLoginRequest("name@subdomain.email.com", "name"))
            .andExpect(content().string(HttpStatus.OK.getReasonPhrase()))
            .andExpect(header().string("Set-Cookie", matchesPattern("fetch-access-token=\\S+;.*")))
            .andExpect(status().isOk());
    }

    @Test
    public void testLogInWithValidSymbols() throws Exception {
        mockMvc.perform(buildLoginRequest("name*$%@email.com", "name"))
            .andExpect(content().string(HttpStatus.OK.getReasonPhrase()))
            .andExpect(header().string("Set-Cookie", matchesPattern("fetch-access-token=\\S+;.*")))
            .andExpect(status().isOk());
    }

    @Test
    public void testLogInWithMinimalInfo() throws Exception {
        mockMvc.perform(buildLoginRequest("a@a.aa", "a"))
            .andExpect(content().string(HttpStatus.OK.getReasonPhrase()))
            .andExpect(header().string("Set-Cookie", matchesPattern("fetch-access-token=\\S+;.*")))
            .andExpect(status().isOk());
    }

    @Test
    public void testLogInWithBlankInfo1() throws Exception {
        mockMvc.perform(buildLoginRequest(null, " "))
            .andExpect(status().isUnprocessableEntity());
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    @Test
    public void testLogInWithBlankInfo2() throws Exception {
        var request = post(AuthController.LOGIN_PATH)
            .contentType("application/json")
            .content(new String("{\"email\":\"e@mail.com\",\"name\":}"));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "              "})
    public void testLogInWithEmptyBody(String body) throws Exception {
        var request = post(AuthController.LOGIN_PATH)
            .contentType("application/json")
            .content(body);
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testLogInWithInvalidFieldType() throws Exception {
        var request = post(AuthController.LOGIN_PATH)
            .contentType("application/json")
            .content("{\"email\":32435235352,\"name\":\"name\"}");
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testLogInWithInvalidSymbols() throws Exception {
        mockMvc.perform(buildLoginRequest("%@#$^@email.com", "%@#$^"))
            .andExpect(status().isUnprocessableEntity());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"email\":\"e@mail.com\"}"})
    public void testLogInWithMissingFields(String body) throws Exception {
        var request = post(AuthController.LOGIN_PATH)
            .contentType("application/json")
            .content(body);
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testLogInWithReallyLongName() throws Exception {
        mockMvc.perform(buildLoginRequest("name@email.com", "n".repeat(1000)))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testLogOut() throws Exception {
        var cookies = mockMvc.perform(buildLoginRequest("test@email.com", "test"))
            .andReturn()
            .getResponse()
            .getCookies();
        mockMvc.perform(post(AuthController.LOGOUT_PATH).cookie(cookies))
            .andExpect(content().string(HttpStatus.OK.getReasonPhrase()))
            .andExpect(status().isOk());
    }

    @Test
    public void testLogOutWithoutLogIn() throws Exception {
        mockMvc.perform(post(AuthController.LOGOUT_PATH)).andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogOutWithBrokenCookie() throws Exception {
        var cookies = mockMvc.perform(buildLoginRequest("test@email.com", "test"))
            .andReturn()
            .getResponse()
            .getCookies();
        cookies[0].setValue("invalid");
        mockMvc.perform(post(AuthController.LOGOUT_PATH).cookie(cookies))
            .andExpect(status().isUnauthorized());
    }
}

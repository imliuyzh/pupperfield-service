package com.pupperfield.backend.controller;

import com.pupperfield.backend.model.LoginRequestDto;
import com.pupperfield.backend.service.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthController authController;

    @Test
    public void testLogin() {
        String email = "name@email.com", name = "name";
        given(tokenService.generate(any(String.class), any(String.class))).willReturn("token");

        var response = authController.logIn(new LoginRequestDto(email, name));
        verify(tokenService, times(1)).generate(
            ArgumentMatchers.eq(email), ArgumentMatchers.eq(name));

        var cookie = response.getHeaders().getOrDefault("Set-Cookie", null);
        assertThat(response.getBody()).isEqualTo(HttpStatus.OK.getReasonPhrase());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cookie).isNotEmpty();
        assertThat(cookie.getFirst()).startsWith("fetch-access-token=token");
    }

    @Test
    public void testLogOut() {
        var response = authController.logOut();
        var cookie = response.getHeaders().getOrDefault("Set-Cookie", null);
        assertThat(response.getBody()).isEqualTo(HttpStatus.OK.getReasonPhrase());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cookie).isNotEmpty();
        assertThat(cookie.getFirst().startsWith("fetch-access-token=token")).isFalse();
    }
}

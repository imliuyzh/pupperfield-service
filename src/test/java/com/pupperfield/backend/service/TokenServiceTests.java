package com.pupperfield.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTests {
    @InjectMocks
    private TokenService tokenService;

    @Test
    public void testGenerateAndValidateToken() {
        var token = tokenService.generate("john.doe@email.com", "John Doe");
        assertThat(token).isNotEmpty();
        assertThat(tokenService.isValid(token)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "        ",
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3"
            + "NTIzOTAzMDgsImV4cCI6MTc4MzkyNjMwOCwiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJv"
            + "Y2tldEBleGFtcGxlLmNvbSIsIkdpdmVuTmFtZSI6IkpvaG5ueSIsIlN1cm5hbWUiOiJSb2NrZXQiLCJ"
            + "FbWFpbCI6Impyb2NrZXRAZXhhbXBsZS5jb20iLCJSb2xlIjpbIk1hbmFnZXIiLCJQcm9qZWN0IEFkbWl"
            + "uaXN0cmF0b3IiXX0.dghMDYh4sJaZt4lhmSPTTiiS0pjJ8gWcEPq4dHskJ4U",
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoibmFtZSIsImVtYWlsIjoibmFtZUBlbWFpbC5j"
            + "b20iLCJleHAiOjE3NTIzOTQyMjIsImlhdCI6MTc1MjM5MDYyMn0.OYmZVWHyTjih8ctgAu-7PVj_bFNi"
            + "COZth0auWPTs0J8",
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoibmFtZSIsImVtYWlsIjoibmFtZUBlbWFpbC5j"
            + "b20iLCJleHAiOjE3NTIzOTQyMjIsImlhdCI6MTc1MjM5MDYyMn0.OYmZVWHyTjih8ctgAu-7PVj_bFNi"
            + "Y2tldEBleGFtcGxlLmNvbSIsIkdpdmVuTmFtZSI6IkpvaG5ueSIsIlN1cm5hbWUiOiJSb2NrZXQiLCJ"
    })
    public void testInvalidToken(String token) {
        assertThat(tokenService.isValid(token)).isFalse();
    }

    @Test
    public void testNullToken() {
        assertThat(tokenService.isValid(null)).isFalse();
    }

    @Test
    public void testUuidAsToken() {
        assertThat(tokenService.isValid(randomUUID().toString())).isFalse();
    }
}

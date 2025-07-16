package com.pupperfield.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {
    @InjectMocks
    private TokenService tokenService;

    @Test
    public void testGenerateAndValidateToken() {
        String token = tokenService.generate("john.doe@email.com", "John Doe");
        assertThat(token).isNotEmpty();
        assertThat(tokenService.isValid(token)).isTrue();
    }

    @Test
    public void testInvalidToken1() {
        assertThat(tokenService.isValid(randomUUID().toString())).isFalse();
    }

    @Test
    public void testInvalidToken2() {
        assertThat(tokenService.isValid("")).isFalse();
    }

    @Test
    public void testInvalidToken3() {
        assertThat(tokenService.isValid("        ")).isFalse();
    }

    @Test
    public void testInvalidToken4() {
        assertThat(tokenService.isValid(
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3"
                + "NTIzOTAzMDgsImV4cCI6MTc4MzkyNjMwOCwiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJv"
                + "Y2tldEBleGFtcGxlLmNvbSIsIkdpdmVuTmFtZSI6IkpvaG5ueSIsIlN1cm5hbWUiOiJSb2NrZXQiLCJ"
                + "FbWFpbCI6Impyb2NrZXRAZXhhbXBsZS5jb20iLCJSb2xlIjpbIk1hbmFnZXIiLCJQcm9qZWN0IEFkbWl"
                + "uaXN0cmF0b3IiXX0.dghMDYh4sJaZt4lhmSPTTiiS0pjJ8gWcEPq4dHskJ4U"
        )).isFalse();
    }

    @Test
    public void testInvalidToken5() {
        assertThat(tokenService.isValid(
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoibmFtZSIsImVtYWlsIjoibmFtZUBlbWFpbC5j"
                + "b20iLCJleHAiOjE3NTIzOTQyMjIsImlhdCI6MTc1MjM5MDYyMn0.OYmZVWHyTjih8ctgAu-7PVj_bFNi"
                + "COZth0auWPTs0J8"
        )).isFalse();
    }

    @Test
    public void testInvalidToken6() {
        assertThat(tokenService.isValid(null)).isFalse();
    }

    @Test
    public void testInvalidToken7() {
        assertThat(tokenService.isValid(
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoibmFtZSIsImVtYWlsIjoibmFtZUBlbWFpbC5j"
                + "b20iLCJleHAiOjE3NTIzOTQyMjIsImlhdCI6MTc1MjM5MDYyMn0.OYmZVWHyTjih8ctgAu-7PVj_bFNi"
                + "Y2tldEBleGFtcGxlLmNvbSIsIkdpdmVuTmFtZSI6IkpvaG5ueSIsIlN1cm5hbWUiOiJSb2NrZXQiLCJ"
                + "uaXN0cmF0b3IiXX0.dghMDYh4sJaZt4lhmSPTTiiS0pjJ8gWcEPq4dHskJ4U"
        )).isFalse();
    }
}

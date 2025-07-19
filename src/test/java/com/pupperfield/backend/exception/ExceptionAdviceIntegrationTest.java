package com.pupperfield.backend.exception;

import com.pupperfield.backend.controller.AuthController;
import com.pupperfield.backend.controller.DogController;
import com.pupperfield.backend.controller.StatusController;
import com.pupperfield.backend.filter.AuthFilter;
import com.pupperfield.backend.service.DogService;
import com.pupperfield.backend.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.pupperfield.backend.auth.AuthRequestBuilder.buildLoginRequest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
    AuthController.class,
    AuthFilter.class,
    DogController.class,
    DogService.class,
    StatusController.class,
    TokenService.class
})
@WebMvcTest(ExceptionAdvice.class)
public class ExceptionAdviceIntegrationTest {
    @MockitoBean
    private DogService dogService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatusController statusController;

    @Test
    public void testInvalidPath() throws Exception {
        mockMvc.perform(get("/status/invalid-path")).andExpect(status().isNotFound());
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    @Test
    public void testBrokenBody() throws Exception {
        var cookies = mockMvc.perform(buildLoginRequest("test@email.com", "test"))
            .andReturn()
            .getResponse()
            .getCookies();
        var request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content(new String("["))
            .cookie(cookies);
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testEmptyStringArray() throws Exception {
        var cookies = mockMvc.perform(buildLoginRequest("test@email.com", "test"))
            .andReturn()
            .getResponse()
            .getCookies();
        var request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content("[\"\"]")
            .cookie(cookies);
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testInvalidFromParameter() throws Exception {
        var cookies = mockMvc.perform(buildLoginRequest("test@email.com", "test"))
            .andReturn()
            .getResponse()
            .getCookies();
        var request =
            get("%s?from=bbwbwe".formatted(DogController.DOG_SEARCH_PATH)).cookie(cookies);
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testInvalidSortParameter() throws Exception {
        var cookies = mockMvc.perform(buildLoginRequest("test@email.com", "test"))
            .andReturn()
            .getResponse()
            .getCookies();
        mockMvc.perform(get("%s?sort=".formatted(DogController.DOG_SEARCH_PATH)).cookie(cookies))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testUnauthorized() throws Exception {
        mockMvc.perform(get(DogController.DOG_BREEDS_PATH)).andExpect(status().isUnauthorized());
    }

    @Test
    public void testWrongMediaType() throws Exception {
        var request = post(AuthController.LOGIN_PATH)
            .contentType("application/text")
            .content("[]");
        mockMvc.perform(request).andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void testWrongMethod() throws Exception {
        mockMvc.perform(delete(AuthController.LOGIN_PATH))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testInternalServerError() throws Exception {
        given(statusController.report()).willAnswer(invocation -> {
            throw new Exception();
        });
        mockMvc.perform(get(StatusController.STATUS_PATH))
            .andExpect(status().isInternalServerError());
        verify(statusController, times(1)).report();
    }

    @Test
    public void testOptionsMethodWillNotThrowAuthException() {
        assertDoesNotThrow(() -> {
            mockMvc.perform(options(AuthController.LOGIN_PATH)).andExpect(status().isOk());
            mockMvc.perform(options(AuthController.LOGOUT_PATH)).andExpect(status().isOk());
            mockMvc.perform(options(DogController.DOGS_PATH)).andExpect(status().isOk());
            mockMvc.perform(options(DogController.DOG_BREEDS_PATH)).andExpect(status().isOk());
            mockMvc.perform(options(DogController.DOG_MATCH_PATH)).andExpect(status().isOk());
            mockMvc.perform(options(DogController.DOG_SEARCH_PATH)).andExpect(status().isOk());
            mockMvc.perform(options(StatusController.STATUS_PATH)).andExpect(status().isOk());
        });
    }
}

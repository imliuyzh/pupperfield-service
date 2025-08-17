package com.pupperfield.backend.advice;

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

import static com.pupperfield.backend.auth.AuthRequestBuilder.getAuthCookie;
import static com.pupperfield.backend.constant.AuthConstants.LOGIN_PATH;
import static com.pupperfield.backend.constant.AuthConstants.LOGOUT_PATH;
import static com.pupperfield.backend.constant.DogConstants.DOGS_PATH;
import static com.pupperfield.backend.constant.DogConstants.DOG_BREEDS_PATH;
import static com.pupperfield.backend.constant.DogConstants.DOG_MATCH_PATH;
import static com.pupperfield.backend.constant.DogConstants.DOG_SEARCH_PATH;
import static com.pupperfield.backend.constant.StatusConstants.STATUS_PATH;
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
public class ExceptionAdviceIntegrationTests {
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
        var request = post(DOG_MATCH_PATH)
            .contentType("application/json")
            .content(new String("["))
            .cookie(getAuthCookie(mockMvc, "test@email.com", "test"));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testEmptyStringArray() throws Exception {
        var request = post(DOG_MATCH_PATH)
            .contentType("application/json")
            .content("[\"\"]")
            .cookie(getAuthCookie(mockMvc, "test@email.com", "test"));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testInvalidFromParameter() throws Exception {
        var request = get(DOG_SEARCH_PATH + "?from={from}", "bbwbwe")
            .cookie(getAuthCookie(mockMvc, "test@email.com", "test"));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testInvalidSortParameter() throws Exception {
        var request = get(DOG_SEARCH_PATH + "?sort=")
            .cookie(getAuthCookie(mockMvc, "test@email.com", "test"));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testUnauthorized() throws Exception {
        mockMvc.perform(get(DOG_BREEDS_PATH)).andExpect(status().isUnauthorized());
    }

    @Test
    public void testWrongMediaType() throws Exception {
        var request = post(LOGIN_PATH)
            .contentType("application/text")
            .content("[]");
        mockMvc.perform(request).andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void testWrongMethod() throws Exception {
        mockMvc.perform(delete(LOGIN_PATH)).andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testInternalServerError() throws Exception {
        given(statusController.report()).willAnswer(invocation -> {
            throw new Exception();
        });
        mockMvc.perform(get(STATUS_PATH)).andExpect(status().isInternalServerError());
        verify(statusController, times(1)).report();
    }

    @Test
    public void testOptionsMethodWillNotBeUnauthorized() throws Exception {
        mockMvc.perform(options(LOGIN_PATH)).andExpect(status().isOk());
        mockMvc.perform(options(LOGOUT_PATH)).andExpect(status().isOk());
        mockMvc.perform(options(DOGS_PATH)).andExpect(status().isOk());
        mockMvc.perform(options(DOG_BREEDS_PATH)).andExpect(status().isOk());
        mockMvc.perform(options(DOG_MATCH_PATH)).andExpect(status().isOk());
        mockMvc.perform(options(DOG_SEARCH_PATH)).andExpect(status().isOk());
        mockMvc.perform(options(STATUS_PATH)).andExpect(status().isOk());
    }
}

package com.pupperfield.backend.controller;

import com.pupperfield.backend.filter.AuthFilter;
import com.pupperfield.backend.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({AuthFilter.class, TokenService.class})
@WebMvcTest(StatusController.class)
public class StatusControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testStatusReport() throws Exception {
        mockMvc.perform(get(StatusController.STATUS_PATH))
            .andExpect(content().string(HttpStatus.OK.getReasonPhrase()))
            .andExpect(status().isOk());
    }
}

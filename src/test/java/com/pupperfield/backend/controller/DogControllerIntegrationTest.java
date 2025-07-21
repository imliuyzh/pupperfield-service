package com.pupperfield.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pupperfield.backend.model.DogSearchResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.pupperfield.backend.auth.AuthRequestBuilder.getAuthCookie;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.oneOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class DogControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "dog.controller@email.com";
    private static final String TEST_NAME = "DogController";

    @Test
    public void testGetBreeds() throws Exception {
        var request = get(DogController.DOG_BREEDS_PATH).cookie(
            getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn();

        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        var breeds = objectMapper.readValue(
            response.getResponse().getContentAsString(), String[].class);
        assertThat(breeds).isNotEmpty();
        for (var breed : breeds) {
            assertThat(breed).isNotEmpty();
        }
    }

    @Test
    public void testMatch() throws Exception {
        String[] idList = {
            "z7_-OZUBBPFf4ZNZzPlX",
            "vcD-OZUBBPFf4ZNZzA1l",
            "zb_-OZUBBPFf4ZNZzP1a",
            "HMD-OZUBBPFf4ZNZzApi",
            "QsD-OZUBBPFf4ZNZzCx-",
            "9L_-OZUBBPFf4ZNZzPdW",
            "s8D-OZUBBPFf4ZNZzC1_",
            "PcD-OZUBBPFf4ZNZzBds",
            "IcD-OZUBBPFf4ZNZzC-A",
            "5MD-OZUBBPFf4ZNZzCd7"
        };
        var request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(idList))
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.match").exists())
            .andExpect(jsonPath("$.match").value(oneOf(idList)));
    }

    @Test
    public void testMatchWithAllIds() throws Exception {
        var cookies = getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME);
        var request = get(DogController.DOG_SEARCH_PATH)
            .param("size", Integer.toString(Integer.MAX_VALUE))
            .cookie(cookies);
        var response = objectMapper.readValue(
            mockMvc.perform(request).andReturn().getResponse().getContentAsString(),
            DogSearchResponseDto.class
        );
        request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(response.getResultIds()))
            .cookie(cookies);
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.match").exists())
            .andExpect(jsonPath("$.match").value(oneOf(response.getResultIds().toArray())));
    }

    @Test
    public void testMatchWithEmptyContent1() throws Exception {
        var request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content("")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testMatchWithEmptyContent2() throws Exception {
        var request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content("               ")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testMatchWithEmptyList() throws Exception {
        var request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content("[]")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testMatchWithInvalidIds1() throws Exception {
        var idList = List.of(
            "z7_-OZUBBPFf4ZNZzPlX",
            "vcD-OZUBBPFf4ZNZzA1l",
            "zb_-OZUBBPFf4ZNZzP1a",
            " ",
            "QsD-OZUBBPFf4ZNZzCx-",
            "",
            "s8D-OZUBBPFf4ZNZzC1_",
            "                    ",
            "IcD-OZUBBPFf4ZNZzC-A",
            "5MD-OZUBBPFf4ZNZzCd7"
        );
        var request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(idList))
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testMatchWithInvalidIds2() throws Exception {
        var idList = List.of("id1".repeat(100), "id2".repeat(200));
        var request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(idList))
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testMatchWithInvalidIds3() throws Exception {
        var request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content("[\"z7_-OZUBBPFf4ZNZzPlX\", null, \"zb_-OZUBBPFf4ZNZzP1a\", "
                + "null, \"QsD-OZUBBPFf4ZNZzCx-\"]")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testMatchWithMissingBody() throws Exception {
        var request = post(DogController.DOG_MATCH_PATH)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testMatchWithOneId() throws Exception {
        var request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content("[\"MMD-OZUBBPFf4ZNZzCl8\"]")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.match").exists())
            .andExpect(jsonPath("$.match").value("MMD-OZUBBPFf4ZNZzCl8"));
    }

    @Test
    public void testMatchWithSameIds() throws Exception {
        var request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content("[\"MMD-OZUBBPFf4ZNZzCl8\", \"MMD-OZUBBPFf4ZNZzCl8\"]")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.match").exists())
            .andExpect(jsonPath("$.match").value("MMD-OZUBBPFf4ZNZzCl8"));
    }
}

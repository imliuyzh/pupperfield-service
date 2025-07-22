package com.pupperfield.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pupperfield.backend.model.DogDto;
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
public class DogControllerIntegrationTests {
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
    public void testList() throws Exception {
        var idList = List.of(
            "qcD-OZUBBPFf4ZNZzDCC",
            "qsD-OZUBBPFf4ZNZzDCC",
            "tcD-OZUBBPFf4ZNZzDCC",
            "wMD-OZUBBPFf4ZNZzDCC",
            "w8D-OZUBBPFf4ZNZzDCC",
            "y8D-OZUBBPFf4ZNZzDCC",
            "zcD-OZUBBPFf4ZNZzDCC",
            "zsD-OZUBBPFf4ZNZzDCC",
            "08D-OZUBBPFf4ZNZzDCC",
            "18D-OZUBBPFf4ZNZzDCC"
        );
        var request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(idList))
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        List<DogDto> dogs = objectMapper.readValue(
            response.getContentAsString(),
            new TypeReference<>() {
            }
        );
        for (var index = 0; index < idList.size(); index++) {
            assertThat(dogs.get(index).getId()).isEqualTo(idList.get(index));
        }
    }

    @Test
    public void testListWith1Id() throws Exception {
        var request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content("[\"ccD-OZUBBPFf4ZNZzANe\"]")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value("ccD-OZUBBPFf4ZNZzANe"));
    }

    @Test
    public void testListWith100Ids() throws Exception {
        var cookies = getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME);
        var request = get(DogController.DOG_SEARCH_PATH)
            .param("size", "100")
            .cookie(cookies);
        var response = objectMapper.readValue(
            mockMvc.perform(request).andReturn().getResponse().getContentAsString(),
            DogSearchResponseDto.class
        );
        request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(response.getResultIds()))
            .cookie(cookies);
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(100));
    }

    @Test
    public void testListWithManyIds() throws Exception {
        var cookies = getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME);
        var request = get(DogController.DOG_SEARCH_PATH)
            .param("from", "1000")
            .param("size", "500")
            .cookie(cookies);
        var response = objectMapper.readValue(
            mockMvc.perform(request).andReturn().getResponse().getContentAsString(),
            DogSearchResponseDto.class
        );
        request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(response.getResultIds()))
            .cookie(cookies);
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testListWithEmptyArray() throws Exception {
        var request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content("[]")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testListWithEmptyContent1() throws Exception {
        var request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content("")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testListWithEmptyContent2() throws Exception {
        var request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content("               ")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testListWithInvalidId1() throws Exception {
        var request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content("[\"%s\", \"w8D-OZUBBPFf4ZNZzDCC\"]".formatted("a".repeat(21)))
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testListWithInvalidId2() throws Exception {
        var request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content("[\"w8D-OZUBBPFf4ZNZzDCC\", null]")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testListWithInvalidId3() throws Exception {
        var request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content("[\"w8D-OZUBBPFf4ZNZzDCC\", \"\"]")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testListWithInvalidId4() throws Exception {
        var request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content("[\"w8D-OZUBBPFf4ZNZzDCC\", \"    \"]")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testListWithMissingBody() throws Exception {
        var request = post(DogController.DOGS_PATH)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testListWithNoResult() throws Exception {
        var request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content("[\"qcD\", \"OZUBBPFf4ZNZzDCC\"]")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testListWithRepeatedIds() throws Exception {
        var request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content("[\"qcD-OZUBBPFf4ZNZzDCC\", \"qcD-OZUBBPFf4ZNZzDCC\"]")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value("qcD-OZUBBPFf4ZNZzDCC"));
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
    public void testMatchWithRepeatedIds() throws Exception {
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

package com.pupperfield.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pupperfield.backend.model.DogDto;
import com.pupperfield.backend.model.DogSearchResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.pupperfield.backend.auth.AuthRequestBuilder.getAuthCookie;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.oneOf;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class DogControllerIntegrationTests {
    private static final String TEST_EMAIL = "dog.controller@email.com";
    private static final String TEST_NAME = "DogController";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
            response.getContentAsString(), new TypeReference<>() {
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
        var request = get(DogController.DOG_SEARCH_PATH + "?size={size}", 100).cookie(cookies);
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

    @ParameterizedTest
    @ValueSource(strings = {"age:asc", "age:desc"})
    public void testListWithAgeLimit(String sort) throws Exception {
        var path = "%s?ageMax={ageMax}&ageMin={ageMin}&size={size}&sort={sort}"
            .formatted(DogController.DOG_SEARCH_PATH);
        var cookies = getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME);
        var request = get(path, 3, 3, 3, sort).cookie(cookies);
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
            .andExpect(jsonPath("$[*].age").value(everyItem(equalTo(3))));
    }

    @Test
    public void testListWithManyIds() throws Exception {
        var cookies = getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME);
        var request = get(DogController.DOG_SEARCH_PATH + "?from={from}&size={size}", 1000, 500)
            .cookie(cookies);
        var response = objectMapper.readValue(
            mockMvc.perform(request).andReturn().getResponse().getContentAsString(),
            DogSearchResponseDto.class);
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

    @ParameterizedTest
    @ValueSource(strings = {"", "               "})
    public void testListWithEmptyContent(String content) throws Exception {
        var request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content(content)
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
            .andExpect(jsonPath("$.match").value(oneOf(idList)));
    }

    @Test
    public void testMatchWithAllIds() throws Exception {
        var cookies = getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME);
        var request = get(DogController.DOG_SEARCH_PATH + "?size={size}", Integer.MAX_VALUE)
            .cookie(cookies);
        var response = objectMapper.readValue(
            mockMvc.perform(request).andReturn().getResponse().getContentAsString(),
            DogSearchResponseDto.class);
        request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(response.getResultIds()))
            .cookie(cookies);
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.match").value(oneOf(response.getResultIds().toArray())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "               "})
    public void testMatchWithEmptyContent(String content) throws Exception {
        var request = post(DogController.DOG_MATCH_PATH)
            .contentType("application/json")
            .content(content)
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
            .andExpect(jsonPath("$.match").value("MMD-OZUBBPFf4ZNZzCl8"));
    }

    @Test
    public void testSearch() throws Exception {
        var path = "%s?ageMax=100&ageMin=0&breeds=%s&from=0&size=10&sort=%s&zipCodes=%s".formatted(
            DogController.DOG_SEARCH_PATH,
            "Great Dane,Norwegian Elkhound,Standard Poodle",
            "name:asc",
            "72080,01053,59634"
        );
        var request = get(path).cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNull(result.getNext());
        assertNull(result.getPrevious());
        assertEquals(3, result.getResultIds().size());
        assertTrue(result.getTotal() < 1000);
    }

    @Test
    public void testSearchPagingOffset1() throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?size=11")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/dogs/search?size=11&from=11", result.getNext());
        assertNull(result.getPrevious());
        assertEquals(11, result.getResultIds().size());
        assertTrue(result.getTotal() > 0);
    }

    @Test
    public void testSearchPagingOffset2() throws Exception {
        var request = get("%s?from=101&size=6".formatted(DogController.DOG_SEARCH_PATH))
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/dogs/search?from=107&size=6", result.getNext());
        assertEquals("/dogs/search?from=95&size=6", result.getPrevious());
        assertEquals(6, result.getResultIds().size());
        assertTrue(result.getTotal() > 0);
    }

    @Test
    public void testSearchPagingOffset3() throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?size=1&from={from}", Integer.MAX_VALUE)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNull(result.getNext());
        assertEquals(
            "/dogs/search?size=1&from=%d".formatted(Integer.MAX_VALUE - 1),
            result.getPrevious());
        assertEquals(0, result.getResultIds().size());
        assertTrue(result.getTotal() > 0);
    }

    @Test
    public void testSearchWithAgeMax1() throws Exception {
        var cookies = getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME);
        var request = get(DogController.DOG_SEARCH_PATH + "?ageMax=10&size=1&sort=age:desc")
            .cookie(cookies);
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result1 = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/dogs/search?ageMax=10&size=1&sort=age:desc&from=1", result1.getNext());
        assertNull(result1.getPrevious());
        assertEquals(1, result1.getResultIds().size());
        assertTrue(result1.getTotal() > 0);

        request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(result1.getResultIds()))
            .cookie(cookies);
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].age").value(10));
    }

    @Test
    public void testSearchWithAgeMax2() throws Exception {
        var cookies = getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME);
        var request = get(DogController.DOG_SEARCH_PATH + "?ageMax=00&size=1&sort=age:desc")
            .cookie(cookies);
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result1 = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/dogs/search?ageMax=00&size=1&sort=age:desc&from=1", result1.getNext());
        assertNull(result1.getPrevious());
        assertEquals(1, result1.getResultIds().size());
        assertTrue(result1.getTotal() > 0);

        request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(result1.getResultIds()))
            .cookie(cookies);
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].age").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-2147483648", "null", "g32g23g3", ",3", ",,,"})
    public void testSearchWithAgeMax3(String ageMax) throws Exception {
        var path = "%s?ageMax=%s&size=1&sort=age:desc".formatted(
            DogController.DOG_SEARCH_PATH, ageMax);
        var request = get(path).cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testSearchWithAgeMin1() throws Exception {
        var cookies = getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME);
        var request = get(DogController.DOG_SEARCH_PATH + "?ageMin=10&size=1&sort=age:asc")
            .cookie(cookies);
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result1 = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/dogs/search?ageMin=10&size=1&sort=age:asc&from=1", result1.getNext());
        assertNull(result1.getPrevious());
        assertEquals(1, result1.getResultIds().size());
        assertTrue(result1.getTotal() > 0);

        request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(result1.getResultIds()))
            .cookie(cookies);
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].age").value(10));
    }

    @Test
    public void testSearchWithAgeMin2() throws Exception {
        var cookies = getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME);
        var request = get(DogController.DOG_SEARCH_PATH + "?ageMax=0&size=1&sort=age:asc")
            .cookie(cookies);
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result1 = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/dogs/search?ageMax=0&size=1&sort=age:asc&from=1", result1.getNext());
        assertNull(result1.getPrevious());
        assertEquals(1, result1.getResultIds().size());
        assertTrue(result1.getTotal() > 0);

        request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(result1.getResultIds()))
            .cookie(cookies);
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].age").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-2147483648", "null", "g32g23g3", ",3", ",,,"})
    public void testSearchWithAgeMin3(String ageMin) throws Exception {
        var path = "%s?ageMin=%s&size=%d&sort=%s".formatted(
            DogController.DOG_SEARCH_PATH, ageMin, 1, "age:desc");
        var request = get(path).cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testSearchWithAgeRange() throws Exception {
        var path = "%s?size=%d&from=%d&ageMax=%d&ageMin=%d".formatted(
            DogController.DOG_SEARCH_PATH, 5, 20, 8, 3);
        var request = get(path).cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/dogs/search?size=5&from=25&ageMax=8&ageMin=3", result.getNext());
        assertEquals("/dogs/search?size=5&from=15&ageMax=8&ageMin=3", result.getPrevious());
        assertEquals(5, result.getResultIds().size());
        assertTrue(result.getTotal() > 0);
    }

    @Test
    public void testSearchWithBreeds1() throws Exception {
        var path = "%s?breeds=Doberman&breeds=West Highland White Terrier&size=5".formatted(
            DogController.DOG_SEARCH_PATH);
        var request = get(path).cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(
            "/dogs/search?breeds=Doberman&breeds=West%20Highland%20White%20Terrier&size=5&from=5",
            result.getNext());
        assertNull(result.getPrevious());
        assertEquals(5, result.getResultIds().size());
        assertTrue(result.getTotal() > 0);
    }

    @Test
    public void testSearchWithBreeds2() throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?breeds={breeds}",
                "80263-adve80263-adve80263-adve,-2147483648")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNull(result.getNext());
        assertNull(result.getPrevious());
        assertEquals(0, result.getResultIds().size());
        assertEquals(0, result.getTotal());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "breedbreedbreedbreedbreedbreedbreed",
        ",3",
        "6,",
        ",,,",
        "       ",
        ""
    })
    public void testSearchWithBreeds3(String zipCodes) throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?zipCodes={zipCodes}", zipCodes)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testSearchWithDefaultValues() throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/dogs/search?size=25&from=25", result.getNext());
        assertNull(result.getPrevious());
        assertEquals(25, result.getResultIds().size());
        assertTrue(result.getTotal() > 0);
    }

    @Test
    public void testSearchWithFrom1() throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?from={from}", 100)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/dogs/search?from=125&size=25", result.getNext());
        assertEquals("/dogs/search?from=75&size=25", result.getPrevious());
        assertEquals(25, result.getResultIds().size());
        assertTrue(result.getTotal() > 0);
    }

    @Test
    public void testSearchWithFrom2() throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?from={from}", 0)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/dogs/search?from=25&size=25", result.getNext());
        assertNull(result.getPrevious());
        assertEquals(25, result.getResultIds().size());
        assertTrue(result.getTotal() > 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-2147483648", "null", "g32g23g3", ",3", ",,,", "       ", ""})
    public void testSearchWithFrom3(String from) throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?from=%s", from)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ageMax", "ageMin", "from", "size"})
    public void testSearchWithIntegerOverflow(String field) throws Exception {
        var path = "%s?%s=%d".formatted(DogController.DOG_SEARCH_PATH, field, Long.MAX_VALUE);
        var request = get(path).cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), mockMvc.perform(request)
            .andReturn()
            .getResponse()
            .getStatus());
    }

    @Test
    public void testSearchWithMultipleParameterValues1() throws Exception {
        var path = "%s?breeds=Saluki&breeds=Doberman&from=351&size=1"
            .formatted(DogController.DOG_SEARCH_PATH);
        var request = get(path).cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNull(result.getNext());
        assertEquals(
            "/dogs/search?breeds=Saluki&breeds=Doberman&from=350&size=1",
            result.getPrevious());
        assertEquals(0, result.getResultIds().size());
        assertEquals(350, result.getTotal());
    }

    @Test
    public void testSearchWithMultipleParameterValues2() throws Exception {
        var cookies = getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME);
        var request = get(DogController.DOG_SEARCH_PATH + "?zipCodes=80263,71341&size=3&from=3")
            .cookie(cookies);
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/dogs/search?zipCodes=80263,71341&size=3&from=6", result.getNext());
        assertEquals("/dogs/search?zipCodes=80263,71341&size=3&from=0", result.getPrevious());
        assertEquals(3, result.getResultIds().size());
        assertEquals(10, result.getTotal());
    }

    @Test
    public void testSearchWithoutResult() throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?ageMax={ageMax}&ageMin={ageMin}", 0, 1)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNull(result.getNext());
        assertNull(result.getPrevious());
        assertEquals(0, result.getResultIds().size());
        assertEquals(0, result.getTotal());
    }

    @Test
    public void testSearchWithSize1() throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?size={size}", 2)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result1 = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/dogs/search?size=2&from=2", result1.getNext());
        assertNull(result1.getPrevious());
        assertEquals(2, result1.getResultIds().size());
        assertTrue(result1.getTotal() > 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-2147483648", "null", "g32g23g3", ",3", ",,,", "       ", ""})
    public void testSearchWithSize2(String size) throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?size={size}", size)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @CsvSource({
        "age:asc,0",
        "age:desc,14",
        "breed:asc,A",
        "breed:desc,Y",
        "name:asc,A",
        "name:desc,Z"
    })
    @ParameterizedTest
    public void testSearchWithSort1(String sort, String firstToken) throws Exception {
        var cookies = getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME);
        var request = get(DogController.DOG_SEARCH_PATH + "?sort={sort}&size={size}", sort, 1)
            .cookie(cookies);
        var result1 = mockMvc.perform(request).andReturn().getResponse();
        var content = objectMapper.readValue(result1.getContentAsString(),
            DogSearchResponseDto.class);

        request = post(DogController.DOGS_PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(content.getResultIds()))
            .cookie(cookies);
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].%s".formatted(sort.split(":")[0]))
                .value(hasToString(startsWith(firstToken))));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "name:",
        ":asc",
        "naame:desc",
        "breed:assc",
        "agge:ascc",
        "zipCode:desc",
        "",
        "avasv",
        "9223372036854775807",
        "name:asc:asc",
        "          ",
        ":::",
        ",,,"
    })
    public void testSearchWithSort2(String sort) throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?sort={sort}", sort)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testSearchWithZipCodes1() throws Exception {
        var path = "%s?zipCodes=80263&zipCodes=71341&size=1&from=8"
            .formatted(DogController.DOG_SEARCH_PATH);
        var request = get(path).cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(
            "/dogs/search?zipCodes=80263&zipCodes=71341&size=1&from=9", result.getNext());
        assertEquals(
            "/dogs/search?zipCodes=80263&zipCodes=71341&size=1&from=7", result.getPrevious());
        assertEquals(1, result.getResultIds().size());
        assertEquals(10, result.getTotal());
    }

    @Test
    public void testSearchWithZipCodes2() throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?zipCodes={zipCodes}", "80263-adve,123")
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        var response = mockMvc.perform(request).andReturn().getResponse();
        var result = objectMapper.readValue(
            response.getContentAsString(), DogSearchResponseDto.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNull(result.getNext());
        assertNull(result.getPrevious());
        assertEquals(0, result.getResultIds().size());
        assertEquals(0, result.getTotal());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "-2147483648",
        ",3",
        "6,",
        ",,,",
        "       ",
        ""
    })
    public void testSearchWithZipCodes3(String zipCodes) throws Exception {
        var request = get(DogController.DOG_SEARCH_PATH + "?zipCodes={zipCodes}", zipCodes)
            .cookie(getAuthCookie(mockMvc, TEST_EMAIL, TEST_NAME));
        mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
    }
}

package com.pupperfield.backend.repository;

import com.pupperfield.backend.config.DatabaseConfig;
import com.pupperfield.backend.pagination.DogSearchPagination;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Import(DatabaseConfig.class)
public class DogRepositoryIntegrationTest {
    @Autowired
    private DogRepository dogRepository;

    @Test
    public void testFindAll() {
        var result = dogRepository.findAll(
            (root, query, builder) -> null,
            new DogSearchPagination(1, 0, Sort.by("name"))
        );
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getId()).isNotEmpty();
        assertThat(result.getTotalElements()).isGreaterThan(0);
    }

    @Test
    public void testGetBreeds() {
        var breeds = dogRepository.getBreeds();
        assertThat(breeds).isNotEmpty();
        for (var breed : breeds) {
            assertThat(breed).isNotEmpty();
        }
    }
}

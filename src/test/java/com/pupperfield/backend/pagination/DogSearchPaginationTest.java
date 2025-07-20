package com.pupperfield.backend.pagination;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DogSearchPaginationTest {
    @Test
    public void testFirst() {
        var pagination = new DogSearchPagination(10, 0, Sort.by(Sort.Direction.ASC, "breed"));
        var result = pagination.first();
        assertThat(pagination.equals(result)).isTrue();
    }

    @Test
    public void testGetPageNumber() {
        var pagination = new DogSearchPagination(5, 5, Sort.by(Sort.Direction.DESC, "age"));
        assertThat(pagination.getPageNumber()).isEqualTo(0);
    }

    @Test
    public void testGetPageSize() {
        var pagination = new DogSearchPagination(7, 70, Sort.by(Sort.Direction.ASC, "name"));
        assertThat(pagination.getPageSize()).isEqualTo(7);
    }

    @Test
    public void testHasPrevious() {
        var pagination = new DogSearchPagination(5, 5, Sort.by(Sort.Direction.DESC, "name"));
        assertThat(pagination.hasPrevious()).isFalse();
    }

    @Test
    public void testNext() {
        var pagination = new DogSearchPagination(100, 500, Sort.by(Sort.Direction.ASC, "age"));
        assertThrows(UnsupportedOperationException.class, pagination::next);
    }

    @Test
    public void testPreviousOrFirst() {
        var pagination = new DogSearchPagination(10, 0, Sort.by(Sort.Direction.DESC, "age"));
        assertThat(pagination.equals(pagination.previousOrFirst())).isTrue();
    }

    @Test
    public void testWithPage() {
        var pagination = new DogSearchPagination(10, 0, Sort.by(Sort.Direction.ASC, "breed"));
        assertThrows(UnsupportedOperationException.class, () -> pagination.withPage(1));
    }
}

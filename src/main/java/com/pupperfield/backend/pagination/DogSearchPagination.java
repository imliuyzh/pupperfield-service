package com.pupperfield.backend.pagination;

import lombok.Getter;
import lombok.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

@Value
public class DogSearchPagination implements Pageable {
    int limit;

    @Getter
    long offset;

    @Getter
    Sort sort;

    @NonNull
    public Pageable first() {
        return new DogSearchPagination(this.limit, this.offset, this.sort);
    }

    public int getPageNumber() {
        return 0;
    }

    public int getPageSize() {
        return this.limit;
    }

    public boolean hasPrevious() {
        return false;
    }

    @NonNull
    public Pageable next() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
    public Pageable previousOrFirst() {
        return this.first();
    }

    @NonNull
    public Pageable withPage(int pageNumber) {
        throw new UnsupportedOperationException("Not implemented");
    }
}

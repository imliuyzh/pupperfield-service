package com.pupperfield.backend.pagination;

import lombok.Getter;
import lombok.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

/**
 * An offset-based pagination implementation for the {@link com.pupperfield.backend.entity.Dog Dog}
 * entities. Note that it cannot go beyond the first page because it is meant to get all the data
 * for the request at once.
 */
@Value
public class DogSearchPagination implements Pageable {
    int limit;

    @Getter
    long offset;

    @Getter
    Sort sort;

    /**
     * Always returns the current page since going after the first page is not supported.
     *
     * @return a pagination object with the same properties
     */
    @NonNull
    public Pageable first() {
        return new DogSearchPagination(this.limit, this.offset, this.sort);
    }

    /**
     * Returns the page number. Since this is offset-based, it will always returns 0.
     *
     * @return 0
     */
    public int getPageNumber() {
        return 0;
    }

    /**
     * Returns the maximum number of items to be returned.
     *
     * @return the page size in the query string
     */
    public int getPageSize() {
        return this.limit;
    }

    /**
     * Indicates whether there is a previous page. It always returns false.
     *
     * @return false
     */
    public boolean hasPrevious() {
        return false;
    }

    /**
     * Not supported in this implementation.
     *
     * @throws UnsupportedOperationException every time
     */
    @NonNull
    public Pageable next() {
        throw new UnsupportedOperationException("Not implemented");
    }


    /**
     * Returns the first page (same as current since paging is not supported).
     *
     * @return a pagination object with the same properties
     */
    @NonNull
    public Pageable previousOrFirst() {
        return this.first();
    }

    /**
     * Not supported in this implementation.
     *
     * @param pageNumber ignored
     * @throws UnsupportedOperationException every time
     */
    @NonNull
    public Pageable withPage(int pageNumber) {
        throw new UnsupportedOperationException("Not implemented");
    }
}

package com.app.meridian.common;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/** The envelope the Next.js screens expect: { data, page, limit, total, totalPages }. */
public record PageResponse<T>(List<T> data, int page, int limit, long total, int totalPages) {

    public static <E, T> PageResponse<T> of(Page<E> result, Function<E, T> mapper) {
        return new PageResponse<>(
                result.getContent().stream().map(mapper).toList(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    public static <T> PageResponse<T> of(Page<T> result) {
        return new PageResponse<>(
                result.getContent(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }
}

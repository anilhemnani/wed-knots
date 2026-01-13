package com.wedknots.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Custom paged response to avoid Spring Data Page serialization issues
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedMessageResponse {
    @JsonProperty("content")
    private List<GuestMessageDTO> content;

    @JsonProperty("totalElements")
    private long totalElements;

    @JsonProperty("totalPages")
    private int totalPages;

    @JsonProperty("currentPage")
    private int currentPage;

    @JsonProperty("pageSize")
    private int pageSize;

    @JsonProperty("hasNext")
    private boolean hasNext;

    @JsonProperty("hasPrevious")
    private boolean hasPrevious;

    /**
     * Convert Spring Data Page to custom paged response
     */
    public static PagedMessageResponse fromPage(Page<GuestMessageDTO> page) {
        return PagedMessageResponse.builder()
            .content(page.getContent())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .currentPage(page.getNumber())
            .pageSize(page.getSize())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}


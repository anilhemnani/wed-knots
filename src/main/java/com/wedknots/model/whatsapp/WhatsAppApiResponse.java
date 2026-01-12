package com.wedknots.model.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * WhatsApp API Response - Wrapper for API list responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppApiResponse<T> {

    private List<T> data;               // Response data (list of items)
    private Paging paging;              // Pagination info

    /**
     * Pagination info
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Paging {
        private Cursors cursors;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Cursors {
            private String before;      // Cursor for previous page
            private String after;       // Cursor for next page
        }
    }

    /**
     * Helper: Check if has data
     */
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }

    /**
     * Helper: Get data count
     */
    public int getDataCount() {
        return data != null ? data.size() : 0;
    }

    /**
     * Helper: Get next cursor
     */
    public String getNextCursor() {
        return paging != null && paging.cursors != null ? paging.cursors.after : null;
    }

    /**
     * Helper: Get previous cursor
     */
    public String getPreviousCursor() {
        return paging != null && paging.cursors != null ? paging.cursors.before : null;
    }
}


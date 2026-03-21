package com.project.wms.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Unified API Response structure for all REST controllers.
 * @param <T> The type of data being returned.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApiResponse<T> {
    private int code; // Response code (e.g., 200 for success)
    private String message; // Human-readable message
    private T data; // The payload data

    /**
     * Helper to create a successful response.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().code(200).message("Success").data(data).build();
    }

    /**
     * Helper to create an error response.
     */
    public static <T> ApiResponse<T> error(int code, String mess) {
        return ApiResponse.<T>builder().code(code).message(mess).build();
    }
}

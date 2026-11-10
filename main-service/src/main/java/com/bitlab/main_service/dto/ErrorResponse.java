package com.bitlab.main_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * The single format of every error returned by the API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response")
public class ErrorResponse {

    private LocalDateTime timestamp;

    @Schema(example = "404")
    private int status;

    @Schema(example = "Not Found")
    private String error;

    @Schema(example = "Course with id 5 not found")
    private String message;

    @Schema(example = "/api/courses/5")
    private String path;

    @Schema(description = "Field name -> validation message. Present only for validation errors.")
    private Map<String, String> validationErrors;
}
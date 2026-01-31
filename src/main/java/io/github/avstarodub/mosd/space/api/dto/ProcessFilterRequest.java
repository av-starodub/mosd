package io.github.avstarodub.mosd.space.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record ProcessFilterRequest(
        @Pattern(regexp = ".*\\S.*", message = "must not be blank")
        @Schema(description = "Filter by process type key", example = "CUT")
        String type,

        @Min(0) @Schema(description = "Zero-based page index", example = "0", defaultValue = "0")
        Integer page,

        @Min(1) @Max(100) @Schema(description = "Page size", example = "20", defaultValue = "50")
        Integer size,

        @Pattern(
                regexp = "(?i)processKey|type|version|createdAt|updatedAt",
                message = "must be one of: processKey, type, version, createdAt, updatedAt")
        @Schema(
                description = "Sort field",
                example = "processKey",
                defaultValue = "processKey",
                allowableValues = {"processKey", "type", "version", "createdAt", "updatedAt"})
        String sortBy,

        @Pattern(regexp = "(?i)asc|desc", message = "must be asc or desc")
        @Schema(
                description = "Sort direction",
                example = "asc",
                defaultValue = "asc",
                allowableValues = {"asc", "desc"})
        String sortDir) {}

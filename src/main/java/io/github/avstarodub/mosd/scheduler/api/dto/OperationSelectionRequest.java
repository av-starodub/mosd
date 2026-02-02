package io.github.avstarodub.mosd.scheduler.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OperationSelectionRequest(
        @NotBlank @Size(max = 50) String operationName,
        @NotBlank @Size(max = 50) String selectedTimeOptionKey) {}

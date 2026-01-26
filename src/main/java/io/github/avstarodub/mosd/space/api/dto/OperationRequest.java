package io.github.avstarodub.mosd.space.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OperationRequest(
        @NotBlank String name, @Positive long executionTime) {}

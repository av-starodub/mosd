package io.github.avstarodub.mosd.space.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OperationRequest(
        @NotBlank @Size(max = 100) String name, @Positive long executionTime) {}

package io.github.avstarodub.mosd.space.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record RateRequest(
        @Positive int units,
        @Positive int minutes,
        @NotBlank String unit) {}

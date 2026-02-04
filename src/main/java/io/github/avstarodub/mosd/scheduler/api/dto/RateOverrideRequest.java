package io.github.avstarodub.mosd.scheduler.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RateOverrideRequest(
        @Positive int units,
        @Positive int minutes,
        @NotBlank @Size(max = 500) String reason) {}

package io.github.avstarodub.mosd.space.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RateOptionRequest(
        @NotBlank String key,
        @NotBlank String title,
        @NotNull @Valid RateRequest rate) {}

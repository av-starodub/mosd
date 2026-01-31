package io.github.avstarodub.mosd.space.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ProcessTypeCreateRequest(
        @NotBlank String type, @NotBlank String title) {}

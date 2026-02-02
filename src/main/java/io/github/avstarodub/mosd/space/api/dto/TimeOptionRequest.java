package io.github.avstarodub.mosd.space.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TimeOptionRequest(
        @NotBlank @Size(max = 50) String key,
        @NotBlank @Size(max = 200) String title,
        @Positive int duration) {}

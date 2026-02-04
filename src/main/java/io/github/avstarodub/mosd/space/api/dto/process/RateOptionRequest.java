package io.github.avstarodub.mosd.space.api.dto.process;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RateOptionRequest(
        @NotBlank @Size(max = 50) String key,
        @NotBlank @Size(max = 200) String title,
        @NotNull @Valid RateRequest rate) {}

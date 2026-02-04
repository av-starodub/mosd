package io.github.avstarodub.mosd.space.api.dto.processtype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProcessTypeCreateRequest(
        @NotBlank @Size(max = 50) String type,
        @NotBlank @Size(max = 200) String title) {}

package io.github.avstarodub.mosd.space.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProcessNewVersionRequest(
        @NotBlank String type,
        String description,
        @NotEmpty @Valid List<OperationRequest> operations,
        @NotNull @Valid RateRequest rate) {}

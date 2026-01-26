package io.github.avstarodub.mosd.space.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProcessRequest(
        @NotBlank String type,
        @NotBlank String title,
        @NotEmpty @Valid List<OperationRequest> operations,
        @NotNull @Valid RateRequest rate) {}

package io.github.avstarodub.mosd.space.api.dto.process;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProcessCreateRequest(
        @NotBlank @Size(max = 50) String processKey,
        @NotBlank @Size(max = 50) String type,
        @Size(max = 500) String description,
        @NotBlank @Size(max = 20) String volumeUnit,
        @NotNull @Positive Integer batchVolume,
        @NotEmpty @Valid List<OperationRequest> operations,
        @NotEmpty @Valid List<RateOptionRequest> rateOptions) {}

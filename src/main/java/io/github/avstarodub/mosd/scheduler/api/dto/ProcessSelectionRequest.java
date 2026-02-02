package io.github.avstarodub.mosd.scheduler.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProcessSelectionRequest(
        @NotBlank @Size(max = 50) String processKey,
        @Positive Integer version,
        @Positive int volume,
        @NotBlank @Size(max = 20) String volumeUnit,
        @Positive Integer batchVolumeOverride,
        @NotBlank @Size(max = 50) String selectedRateOptionKey,
        @NotEmpty @Valid List<OperationSelectionRequest> operationSelections) {}

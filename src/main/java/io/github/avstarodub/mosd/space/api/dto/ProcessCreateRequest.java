package io.github.avstarodub.mosd.space.api.dto;

import io.github.avstarodub.mosd.space.domain.HandoffMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProcessCreateRequest(
        @NotBlank String processKey,
        @NotBlank String type,
        String description,
        @NotBlank String volumeUnit,
        Integer batchVolume,
        @NotNull HandoffMode handoffMode,
        @NotEmpty @Valid List<OperationRequest> operations,
        @NotEmpty @Valid List<RateOptionRequest> rateOptions) {

    @AssertTrue(message = "batchVolume is required for BATCHED and must be null for FULL_ONLY")
    private boolean isBatchVolumeValid() {
        if (handoffMode == null) {
            return false;
        }
        if (handoffMode == HandoffMode.BATCHED) {
            return batchVolume != null && batchVolume > 0;
        }
        return batchVolume == null;
    }
}

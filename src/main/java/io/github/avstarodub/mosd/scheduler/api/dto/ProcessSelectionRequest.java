package io.github.avstarodub.mosd.scheduler.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProcessSelectionRequest(
        @NotBlank @Size(max = 50) String processKey,
        @Positive Integer version,
        @Positive int volume,
        @NotBlank @Size(max = 20) String volumeUnit,
        @Positive Integer batchVolumeOverride,
        @NotNull RateSelectionMode rateSelectionMode,
        @Size(max = 50) String selectedRateOptionKey,
        @Valid RateOverrideRequest rateOverride,
        @NotEmpty @Valid List<OperationSelectionRequest> operationSelections) {
    @AssertTrue(
            message = "Invalid rate selection: OPTION requires selectedRateOptionKey, OVERRIDE requires rateOverride")
    private boolean isRateSelectionValid() {
        if (rateSelectionMode == null) {
            return false;
        }

        boolean hasOptionKey = selectedRateOptionKey != null && !selectedRateOptionKey.isBlank();
        boolean hasOverride = rateOverride != null;

        if (hasOptionKey && hasOverride) {
            return false;
        }

        if (hasOptionKey) {
            return rateSelectionMode == RateSelectionMode.OPTION;
        }

        if (hasOverride) {
            return rateSelectionMode == RateSelectionMode.OVERRIDE;
        }

        return false;
    }
}

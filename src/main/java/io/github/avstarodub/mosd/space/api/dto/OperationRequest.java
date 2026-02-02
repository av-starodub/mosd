package io.github.avstarodub.mosd.space.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OperationRequest(
        @Size(max = 50) String operationType,
        @NotBlank @Size(max = 100) String name,
        @Positive int quantity,
        @NotEmpty @Valid List<TimeOptionRequest> timeOptions) {}

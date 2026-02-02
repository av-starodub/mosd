package io.github.avstarodub.mosd.space.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OperationTypeCreateRequest(
        @NotBlank @Size(max = 50) String type,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 50) String processType,
        @NotEmpty @Valid List<TimeOptionRequest> defaultTimeOptions) {}

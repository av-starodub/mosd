package io.github.avstarodub.mosd.scheduler.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ItemRequest(
        @NotBlank @Size(max = 100) String externalId,
        @NotEmpty @Valid List<ProcessSelectionRequest> processSelections) {}

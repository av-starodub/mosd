package io.github.avstarodub.mosd.scheduler.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record EstimateRequest(
        @NotBlank @Size(max = 100) String orderId,
        @NotEmpty @Valid List<ItemRequest> items) {}

package io.github.avstarodub.mosd.space.api.dto;

import java.time.Instant;
import java.util.List;

public record ProcessResponse(
        String id,
        String processKey,
        int version,
        String type,
        String description,
        List<OperationResponse> operations,
        RateResponse rate,
        Instant createdAt,
        Instant updatedAt) {}

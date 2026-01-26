package io.github.avstarodub.mosd.space.api.dto;

import java.time.Instant;
import java.util.List;

public record ProcessResponse(
        String id,
        String type,
        String title,
        List<OperationResponse> operations,
        RateResponse rate,
        Instant createdAt,
        Instant updatedAt) {}

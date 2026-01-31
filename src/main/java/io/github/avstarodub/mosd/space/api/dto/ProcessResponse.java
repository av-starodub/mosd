package io.github.avstarodub.mosd.space.api.dto;

import io.github.avstarodub.mosd.space.domain.HandoffMode;
import java.time.Instant;
import java.util.List;

public record ProcessResponse(
        String id,
        String processKey,
        int version,
        String type,
        String description,
        String volumeUnit,
        Integer batchVolume,
        HandoffMode handoffMode,
        List<OperationResponse> operations,
        List<RateOptionResponse> rateOptions,
        Instant createdAt,
        Instant updatedAt) {}

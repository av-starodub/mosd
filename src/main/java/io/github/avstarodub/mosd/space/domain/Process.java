package io.github.avstarodub.mosd.space.domain;

import java.time.Instant;
import java.util.List;

public record Process(
        String id,
        String processKey,
        int version,
        String type,
        String description,
        String volumeUnit,
        Integer batchVolume,
        HandoffMode handoffMode,
        List<Operation> operations,
        List<RateOption> rateOptions,
        Instant createdAt,
        Instant updatedAt) {}

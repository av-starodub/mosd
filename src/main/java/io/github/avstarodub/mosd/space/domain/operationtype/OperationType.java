package io.github.avstarodub.mosd.space.domain.operationtype;

import io.github.avstarodub.mosd.space.domain.process.TimeOption;
import java.time.Instant;
import java.util.List;

public record OperationType(
        String id,
        String type,
        String title,
        String processType,
        List<TimeOption> defaultTimeOptions,
        Instant createdAt,
        Instant updatedAt) {}

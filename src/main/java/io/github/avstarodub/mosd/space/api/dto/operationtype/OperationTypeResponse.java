package io.github.avstarodub.mosd.space.api.dto.operationtype;

import io.github.avstarodub.mosd.space.api.dto.common.TimeOptionResponse;
import java.time.Instant;
import java.util.List;

public record OperationTypeResponse(
        String id,
        String type,
        String title,
        String processType,
        List<TimeOptionResponse> defaultTimeOptions,
        Instant createdAt,
        Instant updatedAt) {}

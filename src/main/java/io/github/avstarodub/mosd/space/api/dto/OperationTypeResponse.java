package io.github.avstarodub.mosd.space.api.dto;

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

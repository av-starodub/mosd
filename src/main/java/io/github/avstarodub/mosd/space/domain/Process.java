package io.github.avstarodub.mosd.space.domain;

import java.time.Instant;
import java.util.List;

public record Process(
        String id,
        String processKey,
        int version,
        String type,
        String description,
        List<Operation> operations,
        Rate rate,
        Instant createdAt,
        Instant updatedAt) {}

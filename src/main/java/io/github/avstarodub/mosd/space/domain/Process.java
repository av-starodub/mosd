package io.github.avstarodub.mosd.space.domain;

import java.time.Instant;
import java.util.List;

public record Process(
        String id,
        String type,
        String title,
        List<Operation> operations,
        Rate rate,
        Instant createdAt,
        Instant updatedAt) {}

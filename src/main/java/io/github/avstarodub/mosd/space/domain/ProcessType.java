package io.github.avstarodub.mosd.space.domain;

import java.time.Instant;

public record ProcessType(String id, String type, String title, Instant createdAt, Instant updatedAt) {}

package io.github.avstarodub.mosd.space.api.dto;

import java.time.Instant;

public record ProcessTypeResponse(String id, String type, String title, Instant createdAt, Instant updatedAt) {}

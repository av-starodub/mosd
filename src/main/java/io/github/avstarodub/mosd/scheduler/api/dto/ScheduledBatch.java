package io.github.avstarodub.mosd.scheduler.api.dto;

public record ScheduledBatch(
        String itemExternalId, int batchNumber, int startMinute, int endMinute, int durationMinutes) {}

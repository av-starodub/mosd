package io.github.avstarodub.mosd.scheduler.api.dto;

import java.util.List;

public record QueueEstimate(String processKey, int totalMinutes, List<ScheduledBatch> batches) {}

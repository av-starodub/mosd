package io.github.avstarodub.mosd.scheduler.api.dto;

import java.util.List;

public record EstimateResponse(
        String orderId, int totalMinutes, List<ItemEstimate> items, List<QueueEstimate> queues) {}

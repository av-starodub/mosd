package io.github.avstarodub.mosd.scheduler.api.dto;

import java.util.List;

public record ProcessEstimate(
        String processKey,
        int version,
        int estimatedMinutes,
        int numberOfBatches,
        int minutesPerBatch,
        int operationsMinutes,
        int rateMinutes,
        List<OperationEstimate> operations) {}

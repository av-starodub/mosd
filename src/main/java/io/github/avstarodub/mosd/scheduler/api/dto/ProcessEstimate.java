package io.github.avstarodub.mosd.scheduler.api.dto;

import java.util.List;

public record ProcessEstimate(
        String processKey,
        int version,
        int estimatedMinutes,
        int batchCount,
        int appliedBatchVolume,
        int lastBatchVolume,
        int setupMinutes,
        int productionMinutes,
        RateSelectionUsed rateSelectionUsed,
        List<OperationEstimate> operations) {}

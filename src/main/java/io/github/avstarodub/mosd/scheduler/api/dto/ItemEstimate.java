package io.github.avstarodub.mosd.scheduler.api.dto;

import java.util.List;

public record ItemEstimate(String externalId, int estimatedMinutes, List<ProcessEstimate> processes) {}

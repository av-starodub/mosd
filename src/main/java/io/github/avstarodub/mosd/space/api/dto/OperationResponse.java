package io.github.avstarodub.mosd.space.api.dto;

import java.util.List;

public record OperationResponse(
        String operationType, String name, int quantity, List<TimeOptionResponse> timeOptions) {}

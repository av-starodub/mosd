package io.github.avstarodub.mosd.space.api.dto.process;

import io.github.avstarodub.mosd.space.api.dto.common.TimeOptionResponse;
import java.util.List;

public record OperationResponse(
        String operationType, String name, int quantity, List<TimeOptionResponse> timeOptions) {}

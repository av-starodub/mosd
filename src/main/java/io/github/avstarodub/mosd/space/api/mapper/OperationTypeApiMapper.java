package io.github.avstarodub.mosd.space.api.mapper;

import io.github.avstarodub.mosd.space.api.dto.OperationTypeCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.OperationTypeResponse;
import io.github.avstarodub.mosd.space.api.dto.TimeOptionRequest;
import io.github.avstarodub.mosd.space.api.dto.TimeOptionResponse;
import io.github.avstarodub.mosd.space.domain.OperationType;
import io.github.avstarodub.mosd.space.domain.TimeOption;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OperationTypeApiMapper {

    public static OperationType toDomain(OperationTypeCreateRequest request) {
        return new OperationType(
                null,
                request.type().strip(),
                request.title().strip(),
                request.processType().strip(),
                toTimeOptions(request.defaultTimeOptions()),
                null,
                null);
    }

    public static OperationTypeResponse toResponse(OperationType operationType) {
        return new OperationTypeResponse(
                operationType.id(),
                operationType.type(),
                operationType.title(),
                operationType.processType(),
                toTimeOptionResponses(operationType.defaultTimeOptions()),
                operationType.createdAt(),
                operationType.updatedAt());
    }

    private static List<TimeOption> toTimeOptions(List<TimeOptionRequest> options) {
        return options.stream()
                .map(option ->
                        new TimeOption(option.key().strip(), option.title().strip(), option.duration()))
                .toList();
    }

    private static List<TimeOptionResponse> toTimeOptionResponses(List<TimeOption> options) {
        return options.stream()
                .map(option -> new TimeOptionResponse(option.key(), option.title(), option.duration()))
                .toList();
    }
}

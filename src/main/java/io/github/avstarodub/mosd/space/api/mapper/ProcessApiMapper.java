package io.github.avstarodub.mosd.space.api.mapper;

import io.github.avstarodub.mosd.space.api.dto.OperationRequest;
import io.github.avstarodub.mosd.space.api.dto.OperationResponse;
import io.github.avstarodub.mosd.space.api.dto.ProcessRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessResponse;
import io.github.avstarodub.mosd.space.api.dto.RateRequest;
import io.github.avstarodub.mosd.space.api.dto.RateResponse;
import io.github.avstarodub.mosd.space.domain.Operation;
import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.Rate;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProcessApiMapper {

    public static Process toDomain(ProcessRequest request) {
        return new Process(
                null,
                request.type().strip(),
                request.title().strip(),
                toOperations(request.operations()),
                toRate(request.rate()),
                null,
                null);
    }

    public static ProcessResponse toResponse(Process process) {
        return new ProcessResponse(
                process.id(),
                process.type(),
                process.title(),
                toOperationResponses(process.operations()),
                toRateResponse(process.rate()),
                process.createdAt(),
                process.updatedAt());
    }

    public static Process update(Process existing, Process updated) {
        return new Process(
                existing.id(),
                updated.type(),
                updated.title(),
                updated.operations(),
                updated.rate(),
                existing.createdAt(),
                null);
    }

    private static List<Operation> toOperations(List<OperationRequest> operations) {
        return operations.stream()
                .map(operation -> new Operation(operation.name().strip(), operation.executionTime()))
                .toList();
    }

    private static List<OperationResponse> toOperationResponses(List<Operation> operations) {
        return operations.stream()
                .map(operation -> new OperationResponse(operation.name(), operation.executionTime()))
                .toList();
    }

    private static Rate toRate(RateRequest rate) {
        return new Rate(rate.units(), rate.minutes(), rate.unit().strip());
    }

    private static RateResponse toRateResponse(Rate rate) {
        return new RateResponse(rate.units(), rate.minutes(), rate.unit());
    }
}

package io.github.avstarodub.mosd.space.api.mapper;

import io.github.avstarodub.mosd.space.api.dto.OperationRequest;
import io.github.avstarodub.mosd.space.api.dto.OperationResponse;
import io.github.avstarodub.mosd.space.api.dto.ProcessCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessNewVersionRequest;
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

    public static Process toDomain(ProcessCreateRequest request) {
        return new Process(
                null,
                request.processKey().strip(),
                0,
                request.type().strip(),
                normalizeDescription(request.description()),
                toOperations(request.operations()),
                toRate(request.rate()),
                null,
                null);
    }

    public static Process toDomain(ProcessNewVersionRequest request) {
        return new Process(
                null,
                null,
                0,
                request.type().strip(),
                normalizeDescription(request.description()),
                toOperations(request.operations()),
                toRate(request.rate()),
                null,
                null);
    }

    public static ProcessResponse toResponse(Process process) {
        return new ProcessResponse(
                process.id(),
                process.processKey(),
                process.version(),
                process.type(),
                process.description(),
                toOperationResponses(process.operations()),
                toRateResponse(process.rate()),
                process.createdAt(),
                process.updatedAt());
    }

    public static Process withProcessKey(Process process, String processKey) {
        return new Process(
                process.id(),
                processKey.strip(),
                process.version(),
                process.type(),
                process.description(),
                process.operations(),
                process.rate(),
                process.createdAt(),
                process.updatedAt());
    }

    public static Process withVersion(Process process, int version) {
        return new Process(
                process.id(),
                process.processKey(),
                version,
                process.type(),
                process.description(),
                process.operations(),
                process.rate(),
                process.createdAt(),
                process.updatedAt());
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

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        return description.strip();
    }
}

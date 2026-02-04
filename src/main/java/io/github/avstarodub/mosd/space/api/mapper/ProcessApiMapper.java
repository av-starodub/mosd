package io.github.avstarodub.mosd.space.api.mapper;

import io.github.avstarodub.mosd.space.api.dto.OperationRequest;
import io.github.avstarodub.mosd.space.api.dto.OperationResponse;
import io.github.avstarodub.mosd.space.api.dto.ProcessCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessNewVersionRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessResponse;
import io.github.avstarodub.mosd.space.api.dto.RateOptionRequest;
import io.github.avstarodub.mosd.space.api.dto.RateOptionResponse;
import io.github.avstarodub.mosd.space.api.dto.RateRequest;
import io.github.avstarodub.mosd.space.api.dto.RateResponse;
import io.github.avstarodub.mosd.space.api.dto.TimeOptionRequest;
import io.github.avstarodub.mosd.space.api.dto.TimeOptionResponse;
import io.github.avstarodub.mosd.space.domain.process.Operation;
import io.github.avstarodub.mosd.space.domain.process.Process;
import io.github.avstarodub.mosd.space.domain.process.Rate;
import io.github.avstarodub.mosd.space.domain.process.RateOption;
import io.github.avstarodub.mosd.space.domain.process.TimeOption;
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
                request.volumeUnit().strip(),
                request.batchVolume(),
                toOperations(request.operations()),
                toRateOptions(request.rateOptions()),
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
                request.volumeUnit().strip(),
                request.batchVolume(),
                toOperations(request.operations()),
                toRateOptions(request.rateOptions()),
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
                process.volumeUnit(),
                process.batchVolume(),
                toOperationResponses(process.operations()),
                toRateOptionResponses(process.rateOptions()),
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
                process.volumeUnit(),
                process.batchVolume(),
                process.operations(),
                process.rateOptions(),
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
                process.volumeUnit(),
                process.batchVolume(),
                process.operations(),
                process.rateOptions(),
                process.createdAt(),
                process.updatedAt());
    }

    private static List<Operation> toOperations(List<OperationRequest> operations) {
        return operations.stream().map(ProcessApiMapper::toOperation).toList();
    }

    private static Operation toOperation(OperationRequest operation) {
        return new Operation(
                normalizeOperationType(operation.operationType()),
                operation.name().strip(),
                operation.quantity(),
                toTimeOptions(operation.timeOptions()));
    }

    private static List<OperationResponse> toOperationResponses(List<Operation> operations) {
        return operations.stream().map(ProcessApiMapper::toOperationResponse).toList();
    }

    private static OperationResponse toOperationResponse(Operation operation) {
        return new OperationResponse(
                operation.operationType(),
                operation.name(),
                operation.quantity(),
                toTimeOptionResponses(operation.timeOptions()));
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

    private static List<RateOption> toRateOptions(List<RateOptionRequest> options) {
        return options.stream().map(ProcessApiMapper::toRateOption).toList();
    }

    private static List<RateOptionResponse> toRateOptionResponses(List<RateOption> options) {
        return options.stream().map(ProcessApiMapper::toRateOptionResponse).toList();
    }

    private static RateOption toRateOption(RateOptionRequest option) {
        return new RateOption(option.key().strip(), option.title().strip(), toRate(option.rate()));
    }

    private static RateOptionResponse toRateOptionResponse(RateOption option) {
        return new RateOptionResponse(option.key(), option.title(), toRateResponse(option.rate()));
    }

    private static Rate toRate(RateRequest rate) {
        return new Rate(rate.units(), rate.minutes());
    }

    private static RateResponse toRateResponse(Rate rate) {
        return new RateResponse(rate.units(), rate.minutes());
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        return description.strip();
    }

    private static String normalizeOperationType(String operationType) {
        if (operationType == null) {
            return null;
        }
        return operationType.strip();
    }
}

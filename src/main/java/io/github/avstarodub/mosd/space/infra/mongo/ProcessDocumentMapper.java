package io.github.avstarodub.mosd.space.infra.mongo;

import io.github.avstarodub.mosd.space.domain.Operation;
import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.Rate;
import io.github.avstarodub.mosd.space.domain.RateOption;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProcessDocumentMapper {

    public static ProcessDocument toDocument(Process process) {
        return new ProcessDocument(
                process.id(),
                process.processKey(),
                process.version(),
                process.type(),
                process.description(),
                process.volumeUnit(),
                process.batchVolume(),
                process.handoffMode(),
                toOperationDocuments(process.operations()),
                toRateOptionDocuments(process.rateOptions()),
                process.createdAt(),
                process.updatedAt());
    }

    public static Process toDomain(ProcessDocument document) {
        return new Process(
                document.id(),
                document.processKey(),
                document.version(),
                document.type(),
                document.description(),
                document.volumeUnit(),
                document.batchVolume(),
                document.handoffMode(),
                toOperations(document.operations()),
                toRateOptions(document.rateOptions()),
                document.createdAt(),
                document.updatedAt());
    }

    private static List<OperationDocument> toOperationDocuments(List<Operation> operations) {
        return operations.stream()
                .map(operation -> new OperationDocument(operation.name(), operation.executionTime()))
                .toList();
    }

    private static List<Operation> toOperations(List<OperationDocument> operations) {
        return operations.stream()
                .map(operation -> new Operation(operation.name(), operation.executionTime()))
                .toList();
    }

    private static List<RateOptionDocument> toRateOptionDocuments(List<RateOption> options) {
        return options.stream().map(ProcessDocumentMapper::toRateOptionDocument).toList();
    }

    private static List<RateOption> toRateOptions(List<RateOptionDocument> options) {
        return options.stream().map(ProcessDocumentMapper::toRateOption).toList();
    }

    private static RateOptionDocument toRateOptionDocument(RateOption option) {
        return new RateOptionDocument(option.key(), option.title(), toRateDocument(option.rate()));
    }

    private static RateOption toRateOption(RateOptionDocument option) {
        return new RateOption(option.key(), option.title(), toRate(option.rate()));
    }

    private static RateDocument toRateDocument(Rate rate) {
        return new RateDocument(rate.units(), rate.minutes());
    }

    private static Rate toRate(RateDocument rate) {
        return new Rate(rate.units(), rate.minutes());
    }
}

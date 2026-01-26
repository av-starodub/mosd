package io.github.avstarodub.mosd.space.infra.mongo;

import io.github.avstarodub.mosd.space.domain.Operation;
import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.Rate;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProcessDocumentMapper {

    public static ProcessDocument toDocument(Process process) {
        return new ProcessDocument(
                process.id(),
                process.type(),
                process.title(),
                toOperationDocuments(process.operations()),
                toRateDocument(process.rate()),
                process.createdAt(),
                process.updatedAt());
    }

    public static Process toDomain(ProcessDocument document) {
        return new Process(
                document.id(),
                document.type(),
                document.title(),
                toOperations(document.operations()),
                toRate(document.rate()),
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

    private static RateDocument toRateDocument(Rate rate) {
        return new RateDocument(rate.units(), rate.minutes(), rate.unit());
    }

    private static Rate toRate(RateDocument rate) {
        return new Rate(rate.units(), rate.minutes(), rate.unit());
    }
}

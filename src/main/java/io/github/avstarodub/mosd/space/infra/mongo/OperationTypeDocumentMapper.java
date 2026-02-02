package io.github.avstarodub.mosd.space.infra.mongo;

import io.github.avstarodub.mosd.space.domain.OperationType;
import io.github.avstarodub.mosd.space.domain.TimeOption;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OperationTypeDocumentMapper {

    public static OperationTypeDocument toDocument(OperationType operationType) {
        return new OperationTypeDocument(
                operationType.id(),
                operationType.type(),
                operationType.title(),
                operationType.processType(),
                toTimeOptionDocuments(operationType.defaultTimeOptions()),
                operationType.createdAt(),
                operationType.updatedAt());
    }

    public static OperationType toDomain(OperationTypeDocument document) {
        return new OperationType(
                document.id(),
                document.type(),
                document.title(),
                document.processType(),
                toTimeOptions(document.defaultTimeOptions()),
                document.createdAt(),
                document.updatedAt());
    }

    private static List<TimeOptionDocument> toTimeOptionDocuments(List<TimeOption> options) {
        return options.stream()
                .map(option -> new TimeOptionDocument(option.key(), option.title(), option.duration()))
                .toList();
    }

    private static List<TimeOption> toTimeOptions(List<TimeOptionDocument> options) {
        return options.stream()
                .map(option -> new TimeOption(option.key(), option.title(), option.duration()))
                .toList();
    }
}

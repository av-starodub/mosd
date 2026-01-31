package io.github.avstarodub.mosd.space.infra.mongo;

import io.github.avstarodub.mosd.space.domain.ProcessType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProcessTypeDocumentMapper {

    public static ProcessTypeDocument toDocument(ProcessType type) {
        return new ProcessTypeDocument(type.id(), type.type(), type.title(), type.createdAt(), type.updatedAt());
    }

    public static ProcessType toDomain(ProcessTypeDocument document) {
        return new ProcessType(
                document.id(), document.type(), document.title(), document.createdAt(), document.updatedAt());
    }
}

package io.github.avstarodub.mosd.space.api.mapper;

import io.github.avstarodub.mosd.space.api.dto.processtype.ProcessTypeCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.processtype.ProcessTypeResponse;
import io.github.avstarodub.mosd.space.domain.processtype.ProcessType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProcessTypeApiMapper {

    public static ProcessType toDomain(ProcessTypeCreateRequest request) {
        return new ProcessType(null, request.type().strip(), request.title().strip(), null, null);
    }

    public static ProcessTypeResponse toResponse(ProcessType type) {
        return new ProcessTypeResponse(type.id(), type.type(), type.title(), type.createdAt(), type.updatedAt());
    }
}

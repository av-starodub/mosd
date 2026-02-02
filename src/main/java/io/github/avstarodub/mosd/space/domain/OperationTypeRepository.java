package io.github.avstarodub.mosd.space.domain;

import java.util.List;
import java.util.Optional;

public interface OperationTypeRepository {

    OperationType save(OperationType operationType);

    Optional<OperationType> findByTypeAndProcessType(String type, String processType);

    boolean existsByTypeAndProcessType(String type, String processType);

    List<OperationType> findAllByProcessType(String processType);
}

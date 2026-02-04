package io.github.avstarodub.mosd.space.infra.mongo.operationtype;

import io.github.avstarodub.mosd.space.domain.operationtype.OperationType;
import io.github.avstarodub.mosd.space.domain.operationtype.OperationTypeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MongoOperationTypeRepository implements OperationTypeRepository {

    private final OperationTypeDocumentRepository repository;

    @Override
    public OperationType save(OperationType operationType) {
        var saved = repository.save(OperationTypeDocumentMapper.toDocument(operationType));
        return OperationTypeDocumentMapper.toDomain(saved);
    }

    @Override
    public Optional<OperationType> findByTypeAndProcessType(String type, String processType) {
        return repository.findByTypeAndProcessType(type, processType).map(OperationTypeDocumentMapper::toDomain);
    }

    @Override
    public boolean existsByTypeAndProcessType(String type, String processType) {
        return repository.existsByTypeAndProcessType(type, processType);
    }

    @Override
    public List<OperationType> findAllByProcessType(String processType) {
        return repository.findAllByProcessType(processType).stream()
                .map(OperationTypeDocumentMapper::toDomain)
                .toList();
    }
}

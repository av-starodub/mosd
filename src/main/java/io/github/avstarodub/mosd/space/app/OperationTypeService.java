package io.github.avstarodub.mosd.space.app;

import io.github.avstarodub.mosd.exception.DuplicateResourceException;
import io.github.avstarodub.mosd.exception.ResourceNotFoundException;
import io.github.avstarodub.mosd.space.api.dto.OperationTypeCreateRequest;
import io.github.avstarodub.mosd.space.api.mapper.OperationTypeApiMapper;
import io.github.avstarodub.mosd.space.domain.OperationType;
import io.github.avstarodub.mosd.space.domain.OperationTypeRepository;
import io.github.avstarodub.mosd.space.domain.TimeOption;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationTypeService {

    private final OperationTypeRepository repository;

    private final ProcessTypeService processTypeService;

    public OperationType create(OperationTypeCreateRequest request) {
        var operationType = OperationTypeApiMapper.toDomain(request);
        validateProcessTypeExists(operationType.processType());
        validateUniqueTimeOptionKeys(operationType.defaultTimeOptions());
        return repository.save(operationType);
    }

    public Optional<OperationType> getByTypeAndProcessType(String type, String processType) {
        return repository.findByTypeAndProcessType(type, processType);
    }

    public List<OperationType> getAllByProcessType(String processType) {
        return repository.findAllByProcessType(processType);
    }

    private void validateProcessTypeExists(String processType) {
        if (!processTypeService.existsByType(processType)) {
            throw new ResourceNotFoundException("ProcessType", "type", processType);
        }
    }

    private void validateUniqueTimeOptionKeys(List<TimeOption> options) {
        var seen = new HashSet<String>();
        for (var option : options) {
            var key = option.key();
            if (!seen.add(key)) {
                throw new DuplicateResourceException("TimeOption", "key", key);
            }
        }
    }
}

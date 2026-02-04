package io.github.avstarodub.mosd.space.app;

import io.github.avstarodub.mosd.exception.ResourceNotFoundException;
import io.github.avstarodub.mosd.space.api.dto.processtype.ProcessTypeCreateRequest;
import io.github.avstarodub.mosd.space.api.mapper.ProcessTypeApiMapper;
import io.github.avstarodub.mosd.space.domain.processtype.ProcessType;
import io.github.avstarodub.mosd.space.domain.processtype.ProcessTypeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessTypeService {

    private final ProcessTypeRepository repository;

    public ProcessType create(ProcessTypeCreateRequest request) {
        var type = ProcessTypeApiMapper.toDomain(request);
        return repository.save(type);
    }

    public Optional<ProcessType> getByType(String type) {
        return repository.findByType(type);
    }

    public List<ProcessType> getAll() {
        return repository.findAll();
    }

    public void ensureTypeExists(String type) {
        if (!repository.existsByType(type)) {
            throw new ResourceNotFoundException("ProcessType", "type", type);
        }
    }
}

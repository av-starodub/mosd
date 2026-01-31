package io.github.avstarodub.mosd.space.app;

import io.github.avstarodub.mosd.exception.ResourceInUseException;
import io.github.avstarodub.mosd.space.api.dto.ProcessTypeCreateRequest;
import io.github.avstarodub.mosd.space.api.mapper.ProcessTypeApiMapper;
import io.github.avstarodub.mosd.space.domain.ProcessRepository;
import io.github.avstarodub.mosd.space.domain.ProcessType;
import io.github.avstarodub.mosd.space.domain.ProcessTypeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessTypeService {

    private final ProcessTypeRepository repository;

    private final ProcessRepository processRepository;

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

    public void delete(String type) {
        if (processRepository.existsByType(type)) {
            throw new ResourceInUseException("ProcessType", "type", type);
        }
        repository.deleteByType(type);
    }

    public boolean existsByType(String type) {
        return repository.existsByType(type);
    }
}

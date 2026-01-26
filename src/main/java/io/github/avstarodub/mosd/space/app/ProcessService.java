package io.github.avstarodub.mosd.space.app;

import io.github.avstarodub.mosd.exception.DuplicateResourceException;
import io.github.avstarodub.mosd.space.api.dto.ProcessRequest;
import io.github.avstarodub.mosd.space.api.mapper.ProcessApiMapper;
import io.github.avstarodub.mosd.space.domain.Operation;
import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.ProcessRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessService {

    private final ProcessRepository repository;

    public Process create(ProcessRequest request) {
        var process = ProcessApiMapper.toDomain(request);
        validateUniqueOperationNames(process.operations());
        return repository.save(process);
    }

    public Optional<Process> getById(String id) {
        return repository.findById(id);
    }

    public List<Process> getAll() {
        return repository.findAll();
    }

    public Optional<Process> update(String id, ProcessRequest request) {
        var process = ProcessApiMapper.toDomain(request);
        validateUniqueOperationNames(process.operations());
        return repository
                .findById(id)
                .map(existing -> ProcessApiMapper.update(existing, process))
                .map(repository::save);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    private void validateUniqueOperationNames(List<Operation> operations) {
        var seen = new HashSet<String>();
        for (var operation : operations) {
            var name = operation.name();
            if (!seen.add(name)) {
                throw new DuplicateResourceException("Operation", "name", name);
            }
        }
    }
}

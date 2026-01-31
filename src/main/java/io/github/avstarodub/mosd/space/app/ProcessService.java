package io.github.avstarodub.mosd.space.app;

import io.github.avstarodub.mosd.exception.DuplicateResourceException;
import io.github.avstarodub.mosd.exception.ResourceNotFoundException;
import io.github.avstarodub.mosd.space.api.dto.ProcessCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessNewVersionRequest;
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

    private final ProcessTypeService processTypeService;

    public Process create(ProcessCreateRequest request) {
        var process = ProcessApiMapper.toDomain(request);
        validateTypeExists(process.type());
        validateUniqueOperationNames(process.operations());
        return repository.save(ProcessApiMapper.withVersion(process, 1));
    }

    public Process createVersion(String processKey, ProcessNewVersionRequest request) {
        var latest = repository
                .findLatestByProcessKey(processKey)
                .orElseThrow(() -> new ResourceNotFoundException("Process", "processKey", processKey));
        var process = ProcessApiMapper.withProcessKey(ProcessApiMapper.toDomain(request), processKey);
        validateTypeExists(process.type());
        validateUniqueOperationNames(process.operations());
        return repository.save(ProcessApiMapper.withVersion(process, latest.version() + 1));
    }

    public Optional<Process> getLatestByProcessKey(String processKey) {
        return repository.findLatestByProcessKey(processKey);
    }

    public Optional<Process> getByProcessKeyAndVersion(String processKey, int version) {
        return repository.findByProcessKeyAndVersion(processKey, version);
    }

    public List<Process> getAllLatest() {
        return repository.findAllLatestVersions();
    }

    public List<Process> getAllVersions() {
        return repository.findAllVersions();
    }

    public List<Process> getVersionsByProcessKey(String processKey) {
        return repository.findAllVersionsByProcessKey(processKey);
    }

    public void delete(String processKey) {
        repository.deleteByProcessKey(processKey);
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

    private void validateTypeExists(String type) {
        if (!processTypeService.existsByType(type)) {
            throw new ResourceNotFoundException("ProcessType", "type", type);
        }
    }
}

package io.github.avstarodub.mosd.space.app;

import io.github.avstarodub.mosd.exception.DuplicateResourceException;
import io.github.avstarodub.mosd.exception.ResourceNotFoundException;
import io.github.avstarodub.mosd.space.api.dto.process.ProcessCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.process.ProcessNewVersionRequest;
import io.github.avstarodub.mosd.space.api.mapper.ProcessApiMapper;
import io.github.avstarodub.mosd.space.domain.process.Operation;
import io.github.avstarodub.mosd.space.domain.process.Process;
import io.github.avstarodub.mosd.space.domain.process.ProcessRepository;
import io.github.avstarodub.mosd.space.domain.process.RateOption;
import io.github.avstarodub.mosd.space.domain.process.TimeOption;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessService {

    private final ProcessRepository repository;

    private final ProcessTypeService processTypeService;

    public Process create(ProcessCreateRequest request) {
        var process = ProcessApiMapper.toDomain(request);
        processTypeService.ensureTypeExists(process.type());
        validateUniqueOperationNames(process.operations());
        validateUniqueTimeOptionKeysInOperations(process.operations());
        validateUniqueRateOptionKeys(process.rateOptions());
        return repository.save(ProcessApiMapper.withVersion(process, 1));
    }

    @Retryable(retryFor = DuplicateKeyException.class, backoff = @Backoff(delay = 50))
    public Process createVersion(String processKey, ProcessNewVersionRequest request) {
        var latest = repository
                .findLatestByProcessKey(processKey)
                .orElseThrow(() -> new ResourceNotFoundException("Process", "processKey", processKey));

        var process = ProcessApiMapper.withProcessKey(ProcessApiMapper.toDomain(request), processKey);
        processTypeService.ensureTypeExists(process.type());
        validateUniqueOperationNames(process.operations());
        validateUniqueTimeOptionKeysInOperations(process.operations());
        validateUniqueRateOptionKeys(process.rateOptions());

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

    public List<Process> getAllLatestByType(String type) {
        return repository.findAllLatestVersionsByType(type);
    }

    public List<Process> getVersionsByProcessKey(String processKey) {
        return repository.findAllVersionsByProcessKey(processKey);
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

    private void validateUniqueRateOptionKeys(List<RateOption> options) {
        var seen = new HashSet<String>();
        for (var option : options) {
            var key = option.key();
            if (!seen.add(key)) {
                throw new DuplicateResourceException("RateOption", "key", key);
            }
        }
    }

    private void validateUniqueTimeOptionKeysInOperations(List<Operation> operations) {
        for (var operation : operations) {
            validateUniqueTimeOptionKeys(operation.timeOptions());
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

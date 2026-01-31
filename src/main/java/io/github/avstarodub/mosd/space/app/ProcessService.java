package io.github.avstarodub.mosd.space.app;

import io.github.avstarodub.mosd.exception.DuplicateResourceException;
import io.github.avstarodub.mosd.exception.ResourceNotFoundException;
import io.github.avstarodub.mosd.space.api.dto.ProcessCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessFilterRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessNewVersionRequest;
import io.github.avstarodub.mosd.space.api.mapper.ProcessApiMapper;
import io.github.avstarodub.mosd.space.domain.Operation;
import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.ProcessRepository;
import io.github.avstarodub.mosd.space.domain.RateOption;
import java.util.Comparator;
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
        validateUniqueRateOptionKeys(process.rateOptions());
        return repository.save(ProcessApiMapper.withVersion(process, 1));
    }

    public Process createVersion(String processKey, ProcessNewVersionRequest request) {
        var latest = repository
                .findLatestByProcessKey(processKey)
                .orElseThrow(() -> new ResourceNotFoundException("Process", "processKey", processKey));
        var process = ProcessApiMapper.withProcessKey(ProcessApiMapper.toDomain(request), processKey);
        validateTypeExists(process.type());
        validateUniqueOperationNames(process.operations());
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

    public List<Process> getAllLatestFiltered(ProcessFilterRequest filter) {
        var type = normalizeType(filter.type());
        var processes = type == null ? getAllLatest() : getAllLatestByType(type);
        var sorted = sort(processes, filter.sortBy(), filter.sortDir());
        return paginate(sorted, filter.page(), filter.size());
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

    private void validateUniqueRateOptionKeys(List<RateOption> options) {
        var seen = new HashSet<String>();
        for (var option : options) {
            var key = option.key();
            if (!seen.add(key)) {
                throw new DuplicateResourceException("RateOption", "key", key);
            }
        }
    }

    private void validateTypeExists(String type) {
        if (!processTypeService.existsByType(type)) {
            throw new ResourceNotFoundException("ProcessType", "type", type);
        }
    }

    private List<Process> sort(List<Process> processes, String sortBy, String sortDir) {
        var sortField = normalizeSortBy(sortBy);
        var comparator = comparatorFor(sortField);
        if (isDesc(sortDir)) {
            comparator = comparator.reversed();
        }
        return processes.stream().sorted(comparator).toList();
    }

    private List<Process> paginate(List<Process> processes, Integer page, Integer size) {
        var pageValue = page == null ? 0 : page;
        var sizeValue = size == null ? 50 : size;
        var fromIndex = pageValue * sizeValue;
        if (fromIndex >= processes.size()) {
            return List.of();
        }
        var toIndex = Math.min(fromIndex + sizeValue, processes.size());
        return processes.subList(fromIndex, toIndex);
    }

    private String normalizeType(String type) {
        return type == null ? null : type.strip();
    }

    private String normalizeSortBy(String sortBy) {
        return sortBy == null ? "processkey" : sortBy.strip().toLowerCase();
    }

    private boolean isDesc(String sortDir) {
        return sortDir != null && sortDir.strip().equalsIgnoreCase("desc");
    }

    private Comparator<Process> comparatorFor(String sortBy) {
        return switch (sortBy) {
            case "type" -> Comparator.comparing(Process::type, Comparator.nullsLast(String::compareTo));
            case "version" -> Comparator.comparingInt(Process::version);
            case "createdat" ->
                Comparator.comparing(Process::createdAt, Comparator.nullsLast(Comparator.naturalOrder()));
            case "updatedat" ->
                Comparator.comparing(Process::updatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Process::processKey, Comparator.nullsLast(String::compareTo));
        };
    }
}

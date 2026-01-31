package io.github.avstarodub.mosd.space.infra.mongo;

import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.ProcessRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MongoProcessRepository implements ProcessRepository {

    private final ProcessDocumentRepository repository;

    @Override
    public Process save(Process process) {
        var saved = repository.save(ProcessDocumentMapper.toDocument(process));
        return ProcessDocumentMapper.toDomain(saved);
    }

    @Override
    public Optional<Process> findLatestByProcessKey(String processKey) {
        return repository.findTopByProcessKeyOrderByVersionDesc(processKey).map(ProcessDocumentMapper::toDomain);
    }

    @Override
    public Optional<Process> findByProcessKeyAndVersion(String processKey, int version) {
        return repository.findByProcessKeyAndVersion(processKey, version).map(ProcessDocumentMapper::toDomain);
    }

    @Override
    public boolean existsByType(String type) {
        return repository.existsByType(type);
    }

    @Override
    public List<Process> findAllVersions() {
        var sort = Sort.by(Sort.Order.asc("processKey"), Sort.Order.asc("version"));
        return repository.findAll(sort).stream()
                .map(ProcessDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public List<Process> findAllVersionsByProcessKey(String processKey) {
        return repository.findByProcessKeyOrderByVersionAsc(processKey).stream()
                .map(ProcessDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public List<Process> findAllLatestVersions() {
        return repository.findAllLatestVersions().stream()
                .map(ProcessDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public List<Process> findAllLatestVersionsByType(String type) {
        return repository.findAllLatestVersionsByType(type).stream()
                .map(ProcessDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteByProcessKey(String processKey) {
        repository.deleteByProcessKey(processKey);
    }
}

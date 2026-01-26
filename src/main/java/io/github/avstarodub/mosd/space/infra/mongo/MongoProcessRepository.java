package io.github.avstarodub.mosd.space.infra.mongo;

import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.ProcessRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
    public Optional<Process> findById(String id) {
        return repository.findById(id).map(ProcessDocumentMapper::toDomain);
    }

    @Override
    public List<Process> findAll() {
        return repository.findAll().stream()
                .map(ProcessDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}

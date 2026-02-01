package io.github.avstarodub.mosd.space.infra.mongo;

import io.github.avstarodub.mosd.space.domain.ProcessType;
import io.github.avstarodub.mosd.space.domain.ProcessTypeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MongoProcessTypeRepository implements ProcessTypeRepository {

    private final ProcessTypeDocumentRepository repository;

    @Override
    public ProcessType save(ProcessType type) {
        var saved = repository.save(ProcessTypeDocumentMapper.toDocument(type));
        return ProcessTypeDocumentMapper.toDomain(saved);
    }

    @Override
    public Optional<ProcessType> findByType(String type) {
        return repository.findByType(type).map(ProcessTypeDocumentMapper::toDomain);
    }

    @Override
    public boolean existsByType(String type) {
        return repository.existsByType(type);
    }

    @Override
    public List<ProcessType> findAll() {
        return repository.findAll().stream()
                .map(ProcessTypeDocumentMapper::toDomain)
                .toList();
    }
}

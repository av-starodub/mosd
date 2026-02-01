package io.github.avstarodub.mosd.space.domain;

import java.util.List;
import java.util.Optional;

public interface ProcessTypeRepository {

    ProcessType save(ProcessType type);

    Optional<ProcessType> findByType(String type);

    boolean existsByType(String type);

    List<ProcessType> findAll();
}

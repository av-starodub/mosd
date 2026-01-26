package io.github.avstarodub.mosd.space.domain;

import java.util.List;
import java.util.Optional;

public interface ProcessRepository {

    Process save(Process process);

    Optional<Process> findById(String id);

    List<Process> findAll();

    void deleteById(String id);
}

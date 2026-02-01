package io.github.avstarodub.mosd.space.domain;

import java.util.List;
import java.util.Optional;

public interface ProcessRepository {

    Process save(Process process);

    Optional<Process> findLatestByProcessKey(String processKey);

    Optional<Process> findByProcessKeyAndVersion(String processKey, int version);

    List<Process> findAllVersionsByProcessKey(String processKey);

    void deleteByProcessKey(String processKey);

    List<Process> findAllLatestVersions();

    List<Process> findAllLatestVersionsByType(String type);
}

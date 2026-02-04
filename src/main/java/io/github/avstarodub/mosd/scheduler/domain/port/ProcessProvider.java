package io.github.avstarodub.mosd.scheduler.domain.port;

import io.github.avstarodub.mosd.space.domain.process.Process;
import java.util.Optional;

/**
 * Port interface for Scheduler to access Process data from the Space module.
 * Implemented by Space's ProcessProviderAdapter.
 */
public interface ProcessProvider {

    Optional<Process> findByKey(String processKey);

    Optional<Process> findByKeyAndVersion(String processKey, int version);
}

package io.github.avstarodub.mosd.space.infra.adapter;

import io.github.avstarodub.mosd.scheduler.domain.port.ProcessProvider;
import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.ProcessRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessProviderAdapter implements ProcessProvider {

    private final ProcessRepository repository;

    @Override
    public Optional<Process> findByKey(String processKey) {
        return repository.findLatestByProcessKey(processKey);
    }

    @Override
    public Optional<Process> findByKeyAndVersion(String processKey, int version) {
        return repository.findByProcessKeyAndVersion(processKey, version);
    }
}

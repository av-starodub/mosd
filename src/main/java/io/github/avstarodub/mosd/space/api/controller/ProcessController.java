package io.github.avstarodub.mosd.space.api.controller;

import io.github.avstarodub.mosd.exception.ResourceNotFoundException;
import io.github.avstarodub.mosd.space.api.dto.ProcessCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessNewVersionRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessResponse;
import io.github.avstarodub.mosd.space.api.mapper.ProcessApiMapper;
import io.github.avstarodub.mosd.space.api.paths.SpaceApiPaths;
import io.github.avstarodub.mosd.space.app.ProcessService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(SpaceApiPaths.Processes.BASE)
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcessResponse create(@Valid @RequestBody ProcessCreateRequest request) {
        var created = service.create(request);
        return ProcessApiMapper.toResponse(created);
    }

    @PostMapping(SpaceApiPaths.Processes.VERSIONS)
    @ResponseStatus(HttpStatus.CREATED)
    public ProcessResponse createVersion(
            @PathVariable @NotBlank String processKey, @Valid @RequestBody ProcessNewVersionRequest request) {
        var created = service.createVersion(processKey, request);
        return ProcessApiMapper.toResponse(created);
    }

    @GetMapping(SpaceApiPaths.Processes.BY_KEY)
    @ResponseStatus(HttpStatus.OK)
    public ProcessResponse getLatest(@PathVariable @NotBlank String processKey) {
        return service.getLatestByProcessKey(processKey)
                .map(ProcessApiMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Process", "processKey", processKey));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProcessResponse> getAll(
            @RequestParam(required = false, name = SpaceApiPaths.Processes.TYPE_PARAM) String type) {
        var processes = type == null ? service.getAllLatest() : service.getAllLatestByType(type);
        return processes.stream().map(ProcessApiMapper::toResponse).toList();
    }

    @GetMapping(SpaceApiPaths.Processes.VERSIONS)
    @ResponseStatus(HttpStatus.OK)
    public List<ProcessResponse> getProcessVersions(@PathVariable @NotBlank String processKey) {
        var versions = service.getVersionsByProcessKey(processKey);
        if (versions.isEmpty()) {
            throw new ResourceNotFoundException("Process", "processKey", processKey);
        }
        return versions.stream().map(ProcessApiMapper::toResponse).toList();
    }

    @GetMapping(SpaceApiPaths.Processes.VERSION_BY_NUMBER)
    @ResponseStatus(HttpStatus.OK)
    public ProcessResponse getProcessVersion(
            @PathVariable @NotBlank String processKey, @PathVariable @Positive int version) {
        return service.getByProcessKeyAndVersion(processKey, version)
                .map(ProcessApiMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Process", "version", processKey + ":" + version));
    }
}

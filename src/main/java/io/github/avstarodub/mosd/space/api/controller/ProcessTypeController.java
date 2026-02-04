package io.github.avstarodub.mosd.space.api.controller;

import io.github.avstarodub.mosd.exception.ResourceNotFoundException;
import io.github.avstarodub.mosd.space.api.dto.processtype.ProcessTypeCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.processtype.ProcessTypeResponse;
import io.github.avstarodub.mosd.space.api.mapper.ProcessTypeApiMapper;
import io.github.avstarodub.mosd.space.api.paths.SpaceApiPaths;
import io.github.avstarodub.mosd.space.app.ProcessTypeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(SpaceApiPaths.ProcessTypes.BASE)
@RequiredArgsConstructor
public class ProcessTypeController {

    private final ProcessTypeService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcessTypeResponse create(@Valid @RequestBody ProcessTypeCreateRequest request) {
        var created = service.create(request);
        return ProcessTypeApiMapper.toResponse(created);
    }

    @GetMapping(SpaceApiPaths.ProcessTypes.BY_TYPE)
    @ResponseStatus(HttpStatus.OK)
    public ProcessTypeResponse getByType(@PathVariable @NotBlank String type) {
        return service.getByType(type)
                .map(ProcessTypeApiMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("ProcessType", "type", type));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProcessTypeResponse> getAll() {
        return service.getAll().stream().map(ProcessTypeApiMapper::toResponse).toList();
    }
}

package io.github.avstarodub.mosd.space.api.controller;

import io.github.avstarodub.mosd.exception.ResourceNotFoundException;
import io.github.avstarodub.mosd.space.api.dto.OperationTypeCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.OperationTypeResponse;
import io.github.avstarodub.mosd.space.api.mapper.OperationTypeApiMapper;
import io.github.avstarodub.mosd.space.api.paths.SpaceApiPaths;
import io.github.avstarodub.mosd.space.app.OperationTypeService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(SpaceApiPaths.OperationTypes.BASE)
@RequiredArgsConstructor
public class OperationTypeController {

    private final OperationTypeService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OperationTypeResponse create(@Valid @RequestBody OperationTypeCreateRequest request) {
        var created = service.create(request);
        return OperationTypeApiMapper.toResponse(created);
    }

    @GetMapping(SpaceApiPaths.OperationTypes.BY_TYPE)
    @ResponseStatus(HttpStatus.OK)
    public OperationTypeResponse getByType(
            @PathVariable @NotBlank String type,
            @RequestParam(name = SpaceApiPaths.OperationTypes.PROCESS_TYPE_PARAM) @NotBlank String processType) {
        return service.getByTypeAndProcessType(type, processType)
                .map(OperationTypeApiMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("OperationType", "type", type));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OperationTypeResponse> getAllByProcessType(
            @RequestParam(name = SpaceApiPaths.OperationTypes.PROCESS_TYPE_PARAM) @NotBlank String processType) {
        return service.getAllByProcessType(processType).stream()
                .map(OperationTypeApiMapper::toResponse)
                .toList();
    }
}

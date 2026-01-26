package io.github.avstarodub.mosd.space.api.controller;

import io.github.avstarodub.mosd.exception.ResourceNotFoundException;
import io.github.avstarodub.mosd.space.api.dto.ProcessRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessResponse;
import io.github.avstarodub.mosd.space.api.mapper.ProcessApiMapper;
import io.github.avstarodub.mosd.space.app.ProcessService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/space/processes")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcessResponse create(@Valid @RequestBody ProcessRequest request) {
        var created = service.create(request);
        return ProcessApiMapper.toResponse(created);
    }

    @GetMapping("/{id}")
    public ProcessResponse getById(@PathVariable @NotBlank String id) {
        return service.getById(id)
                .map(ProcessApiMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Process", "id", id));
    }

    @GetMapping
    public List<ProcessResponse> getAll() {
        return service.getAll().stream().map(ProcessApiMapper::toResponse).toList();
    }

    @PutMapping("/{id}")
    public ProcessResponse update(@PathVariable @NotBlank String id, @Valid @RequestBody ProcessRequest request) {
        return service.update(id, request)
                .map(ProcessApiMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Process", "id", id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @NotBlank String id) {
        service.delete(id);
    }
}

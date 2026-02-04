package io.github.avstarodub.mosd.scheduler.api.controller;

import io.github.avstarodub.mosd.scheduler.api.dto.EstimateRequest;
import io.github.avstarodub.mosd.scheduler.api.dto.EstimateResponse;
import io.github.avstarodub.mosd.scheduler.api.paths.SchedulerApiPaths;
import io.github.avstarodub.mosd.scheduler.app.EstimateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(SchedulerApiPaths.BASE)
@RequiredArgsConstructor
public class EstimateController {

    private final EstimateService service;

    @PostMapping(SchedulerApiPaths.ESTIMATE_RELATIVE)
    @ResponseStatus(HttpStatus.OK)
    public EstimateResponse estimate(@Valid @RequestBody EstimateRequest request) {
        return service.estimate(request);
    }
}

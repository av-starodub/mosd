package io.github.avstarodub.mosd.space.api.dto.process;

import jakarta.validation.constraints.Positive;

public record RateRequest(@Positive int units, @Positive int minutes) {}

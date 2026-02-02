package io.github.avstarodub.mosd.space.domain;

import java.util.List;

public record Operation(String operationType, String name, int quantity, List<TimeOption> timeOptions) {}

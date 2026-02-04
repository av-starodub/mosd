package io.github.avstarodub.mosd.space.infra.mongo.process;

import java.util.List;

public record OperationDocument(
        String operationType, String name, int quantity, List<TimeOptionDocument> timeOptions) {}

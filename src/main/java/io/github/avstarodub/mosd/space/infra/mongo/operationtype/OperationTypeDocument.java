package io.github.avstarodub.mosd.space.infra.mongo.operationtype;

import io.github.avstarodub.mosd.space.infra.mongo.process.TimeOptionDocument;
import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("operation_types")
@CompoundIndex(name = "type_process_type_unique", def = "{'type': 1, 'processType': 1}", unique = true)
public record OperationTypeDocument(
        @Id String id,
        String type,
        String title,
        String processType,
        List<TimeOptionDocument> defaultTimeOptions,
        @CreatedDate Instant createdAt,
        @LastModifiedDate Instant updatedAt) {}

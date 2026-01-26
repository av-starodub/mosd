package io.github.avstarodub.mosd.space.infra.mongo;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("processes")
public record ProcessDocument(
        @Id String id,
        String type,
        @Indexed(unique = true) String title,
        List<OperationDocument> operations,
        RateDocument rate,
        @CreatedDate Instant createdAt,
        @LastModifiedDate Instant updatedAt) {}

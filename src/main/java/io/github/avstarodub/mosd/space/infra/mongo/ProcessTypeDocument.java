package io.github.avstarodub.mosd.space.infra.mongo;

import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("process_types")
public record ProcessTypeDocument(
        @Id String id,
        @Indexed(unique = true) String type,
        String title,
        @CreatedDate Instant createdAt,
        @LastModifiedDate Instant updatedAt) {}

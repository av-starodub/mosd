package io.github.avstarodub.mosd.space.infra.mongo.process;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("processes")
@CompoundIndexes(
        @CompoundIndex(name = "process_key_version_unique", def = "{'processKey': 1, 'version': 1}", unique = true))
public record ProcessDocument(
        @Id String id,
        @Indexed String processKey,
        int version,
        @Indexed String type,
        String description,
        String volumeUnit,
        Integer batchVolume,
        List<OperationDocument> operations,
        List<RateOptionDocument> rateOptions,
        @CreatedDate Instant createdAt,
        @LastModifiedDate Instant updatedAt) {}

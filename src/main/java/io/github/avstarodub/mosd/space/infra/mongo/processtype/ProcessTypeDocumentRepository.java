package io.github.avstarodub.mosd.space.infra.mongo.processtype;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessTypeDocumentRepository extends MongoRepository<ProcessTypeDocument, String> {

    Optional<ProcessTypeDocument> findByType(String type);

    boolean existsByType(String type);
}

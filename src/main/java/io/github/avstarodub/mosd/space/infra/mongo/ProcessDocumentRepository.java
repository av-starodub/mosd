package io.github.avstarodub.mosd.space.infra.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessDocumentRepository extends MongoRepository<ProcessDocument, String> {}

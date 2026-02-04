package io.github.avstarodub.mosd.space.infra.mongo.operationtype;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OperationTypeDocumentRepository extends MongoRepository<OperationTypeDocument, String> {

    Optional<OperationTypeDocument> findByTypeAndProcessType(String type, String processType);

    boolean existsByTypeAndProcessType(String type, String processType);

    List<OperationTypeDocument> findAllByProcessType(String processType);
}

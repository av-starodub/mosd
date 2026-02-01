package io.github.avstarodub.mosd.space.infra.mongo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessDocumentRepository extends MongoRepository<ProcessDocument, String> {

    Optional<ProcessDocument> findTopByProcessKeyOrderByVersionDesc(String processKey);

    Optional<ProcessDocument> findByProcessKeyAndVersion(String processKey, int version);

    List<ProcessDocument> findByProcessKeyOrderByVersionAsc(String processKey);

    @Aggregation(
            pipeline = {
                "{$sort: {processKey: 1, version: -1}}",
                "{$group: {_id: '$processKey', doc: {$first: '$$ROOT'}}}",
                "{$replaceRoot: { newRoot: '$doc' }}",
                "{$sort: {processKey: 1}}"
            })
    List<ProcessDocument> findAllLatestVersions();

    @Aggregation(
            pipeline = {
                "{$match: {type: ?0}}",
                "{$sort: {processKey: 1, version: -1}}",
                "{$group: {_id: '$processKey', doc: {$first: '$$ROOT'}}}",
                "{$replaceRoot: { newRoot: '$doc' }}",
                "{$sort: {processKey: 1}}"
            })
    List<ProcessDocument> findAllLatestVersionsByType(String type);
}

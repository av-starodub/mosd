package io.github.avstarodub.mosd.space.testdata;

import static org.instancio.Select.field;

import io.github.avstarodub.mosd.space.api.dto.OperationRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessNewVersionRequest;
import io.github.avstarodub.mosd.space.api.dto.RateRequest;
import io.github.avstarodub.mosd.space.infra.mongo.OperationDocument;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessDocument;
import io.github.avstarodub.mosd.space.infra.mongo.RateDocument;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ModelGenerator {

    private final String processType = "CUT";

    private final String processTypeTitle = "Cutting";

    private final String processTypeB = "WELD";

    private final String processTypeTitleB = "Welding";

    private final String processKeyA = "CUT-01";

    private final String processKeyB = "CUT-02";

    private final String processDescriptionA = "Process A";

    private final String processDescriptionB = "Process B";

    private final String operation1 = "op-1";

    private final String operation2 = "op-2";

    private final String operationX = "op-x";

    private final String unit = "unit";

    private final RateRequest rateDefault = new RateRequest(3, 2, unit);

    private final RateRequest rateUpdate = new RateRequest(2, 1, unit);

    private final RateRequest rateOneToOne = new RateRequest(1, 1, unit);

    private final RateDocument rateDocumentDefault = new RateDocument(3, 2, unit);

    private final Faker faker = new Faker();

    private Model<ProcessNewVersionRequest> processNewVersionRequestModel;

    private Model<ProcessCreateRequest> processCreateRequestModel;

    private Model<ProcessDocument> processDocumentModel;

    @PostConstruct
    private void init() {
        processNewVersionRequestModel = buildNewVersionRequestModel();
        processCreateRequestModel = buildCreateRequestModel();
        processDocumentModel = buildProcessDocumentModel();
    }

    private Model<ProcessNewVersionRequest> buildNewVersionRequestModel() {
        return Instancio.of(ProcessNewVersionRequest.class)
                .set(field(ProcessNewVersionRequest::type), processType)
                .supply(field(ProcessNewVersionRequest::description), () -> faker.commerce()
                        .productName())
                .set(
                        field(ProcessNewVersionRequest::operations),
                        List.of(new OperationRequest(operation1, 5), new OperationRequest(operation2, 10)))
                .set(field(ProcessNewVersionRequest::rate), rateDefault)
                .toModel();
    }

    private Model<ProcessCreateRequest> buildCreateRequestModel() {
        return Instancio.of(ProcessCreateRequest.class)
                .set(field(ProcessCreateRequest::processKey), processKeyA)
                .set(field(ProcessCreateRequest::type), processType)
                .supply(field(ProcessCreateRequest::description), () -> faker.commerce()
                        .productName())
                .set(
                        field(ProcessCreateRequest::operations),
                        List.of(new OperationRequest(operation1, 5), new OperationRequest(operation2, 10)))
                .set(field(ProcessCreateRequest::rate), rateDefault)
                .toModel();
    }

    private Model<ProcessDocument> buildProcessDocumentModel() {
        return Instancio.of(ProcessDocument.class)
                .ignore(field(ProcessDocument::id))
                .ignore(field(ProcessDocument::createdAt))
                .ignore(field(ProcessDocument::updatedAt))
                .set(field(ProcessDocument::processKey), processKeyA)
                .set(field(ProcessDocument::version), 1)
                .set(field(ProcessDocument::type), processType)
                .supply(field(ProcessDocument::description), () -> faker.commerce()
                        .productName())
                .set(
                        field(ProcessDocument::operations),
                        List.of(new OperationDocument(operation1, 5), new OperationDocument(operation2, 10)))
                .set(field(ProcessDocument::rate), rateDocumentDefault)
                .toModel();
    }
}

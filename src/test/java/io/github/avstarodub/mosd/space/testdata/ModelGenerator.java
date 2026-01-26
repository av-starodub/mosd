package io.github.avstarodub.mosd.space.testdata;

import static org.instancio.Select.field;

import io.github.avstarodub.mosd.space.api.dto.OperationRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessRequest;
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

    private final String processTitleA = "Process A";

    private final String processTitleB = "Process B";

    private final String operation1 = "op-1";

    private final String operation2 = "op-2";

    private final String operationX = "op-x";

    private final String unit = "unit";

    private final RateRequest rateDefault = new RateRequest(3, 2, unit);

    private final RateRequest rateUpdate = new RateRequest(2, 1, unit);

    private final RateRequest rateOneToOne = new RateRequest(1, 1, unit);

    private final RateDocument rateDocumentDefault = new RateDocument(3, 2, unit);

    private final Faker faker = new Faker();

    private Model<ProcessRequest> processRequestModel;

    private Model<ProcessDocument> processDocumentModel;

    @PostConstruct
    private void init() {
        processRequestModel = Instancio.of(ProcessRequest.class)
                .set(field(ProcessRequest::type), processType)
                .supply(field(ProcessRequest::title), () -> faker.commerce().productName())
                .set(
                        field(ProcessRequest::operations),
                        List.of(new OperationRequest(operation1, 5), new OperationRequest(operation2, 10)))
                .set(field(ProcessRequest::rate), rateDefault)
                .toModel();

        processDocumentModel = Instancio.of(ProcessDocument.class)
                .ignore(field(ProcessDocument::id))
                .ignore(field(ProcessDocument::createdAt))
                .ignore(field(ProcessDocument::updatedAt))
                .set(field(ProcessDocument::type), processType)
                .supply(field(ProcessDocument::title), () -> faker.commerce().productName())
                .set(
                        field(ProcessDocument::operations),
                        List.of(new OperationDocument(operation1, 5), new OperationDocument(operation2, 10)))
                .set(field(ProcessDocument::rate), rateDocumentDefault)
                .toModel();
    }
}

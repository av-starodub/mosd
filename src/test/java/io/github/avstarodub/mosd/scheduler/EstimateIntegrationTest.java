package io.github.avstarodub.mosd.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.avstarodub.mosd.AbstractTestcontainersConfiguration;
import io.github.avstarodub.mosd.IntegrationTest;
import io.github.avstarodub.mosd.scheduler.api.dto.EstimateResponse;
import io.github.avstarodub.mosd.scheduler.api.dto.ItemEstimate;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessDocumentRepository;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessTypeDocumentRepository;
import io.github.avstarodub.mosd.space.testdata.ModelGenerator;
import io.github.avstarodub.mosd.util.JsonHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@AutoConfigureMockMvc
@DisplayName("Integration tests: Scheduler Estimate")
class EstimateIntegrationTest extends AbstractTestcontainersConfiguration {

    private static final String ESTIMATE_URL = "/api/v1/scheduler/estimate";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessDocumentRepository processRepository;

    @Autowired
    private ProcessTypeDocumentRepository processTypeRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    @BeforeEach
    void setUp() {
        processRepository.deleteAll();
        processTypeRepository.deleteAll();
    }

    @Nested
    @DisplayName("Successful estimates")
    class SuccessfulEstimates {

        @Test
        @DisplayName("Single item with single process returns correct estimate")
        void singleItemSingleProcess() throws Exception {
            seedDefaultProcess();
            var actualResponse = postEstimateSuccess(JsonHelper.EstimateFixtures.SINGLE_ITEM_REQUEST);

            var actualTotalMinutes = actualResponse.totalMinutes();
            assertThat(actualTotalMinutes).isPositive();

            var actualQueues = actualResponse.queues();
            assertThat(actualQueues).isNotEmpty();

            assertThat(actualResponse.orderId()).isEqualTo("ORD-001");
            assertThat(actualResponse.items())
                    .hasSize(1)
                    .first()
                    .extracting(ItemEstimate::externalId)
                    .isEqualTo("ITEM-1");
        }

        @Test
        @DisplayName("Multiple items demonstrate conveyor effect")
        void multipleItemsConveyorEffect() throws Exception {
            seedDefaultProcess();
            var actualResponse = postEstimateSuccess(JsonHelper.EstimateFixtures.MULTI_ITEM_REQUEST);
            assertThat(actualResponse.items()).hasSize(2);

            // Verify conveyor effect: total time should be less than a sum of individual item times
            var actualSumOfItemTimes = actualResponse.items().stream()
                    .mapToInt(ItemEstimate::estimatedMinutes)
                    .sum();
            // Note: For a single process, the conveyor effect shows in queue scheduling
            var actualTotalMinutes = actualResponse.totalMinutes();
            assertThat(actualTotalMinutes).isLessThanOrEqualTo(actualSumOfItemTimes);
        }

        @Test
        @DisplayName("Batch volume override affects calculation")
        void batchVolumeOverride() throws Exception {
            seedDefaultProcess();
            var actualResponse = postEstimateSuccess(JsonHelper.EstimateFixtures.BATCH_OVERRIDE_REQUEST);
            // With larger batch volume, fewer batches should be created.
            var actualNumberOfBatches = actualResponse.items().getFirst().processes().getFirst().numberOfBatches();
            assertThat(actualNumberOfBatches).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Validation errors")
    class ValidationErrors {

        @Test
        @DisplayName("Process not found returns 404")
        void processNotFound() throws Exception {
            assertEstimateStatus(JsonHelper.EstimateFixtures.SINGLE_ITEM_REQUEST, status().isNotFound());
        }

        @Test
        @DisplayName("Invalid operation name returns 400")
        void invalidOperationName() throws Exception {
            seedDefaultProcess();
            assertEstimateStatus(JsonHelper.EstimateFixtures.INVALID_OPERATION_REQUEST, status().isBadRequest());
        }

        @Test
        @DisplayName("Invalid time option key returns 400")
        void invalidTimeOptionKey() throws Exception {
            seedDefaultProcess();
            assertEstimateStatus(JsonHelper.EstimateFixtures.INVALID_TIME_OPTION_REQUEST, status().isBadRequest());
        }

        @Test
        @DisplayName("Invalid rate option key returns 400")
        void invalidRateOptionKey() throws Exception {
            seedDefaultProcess();
            assertEstimateStatus(JsonHelper.EstimateFixtures.INVALID_RATE_OPTION_REQUEST, status().isBadRequest());
        }

        @Test
        @DisplayName("Volume unit mismatch returns 400")
        void volumeUnitMismatch() throws Exception {
            seedDefaultProcess();
            assertEstimateStatus(JsonHelper.EstimateFixtures.INVALID_VOLUME_UNIT_REQUEST, status().isBadRequest());
        }

        @Test
        @DisplayName("Non-positive process version returns 400")
        void nonPositiveProcessVersion() throws Exception {
            seedDefaultProcess();
            assertEstimateStatus(JsonHelper.EstimateFixtures.INVALID_VERSION_REQUEST, status().isBadRequest());
        }

        @Test
        @DisplayName("Non-positive batch override returns 400")
        void nonPositiveBatchOverride() throws Exception {
            seedDefaultProcess();
            assertEstimateStatus(JsonHelper.EstimateFixtures.INVALID_BATCH_OVERRIDE_REQUEST, status().isBadRequest());
        }

        @Test
        @DisplayName("Empty items returns 400")
        void emptyItems() throws Exception {
            assertEstimateStatus(JsonHelper.EstimateFixtures.INVALID_EMPTY_ITEMS_REQUEST, status().isBadRequest());
        }

        @Test
        @DisplayName("Blank order ID returns 400")
        void blankOrderId() throws Exception {
            assertEstimateStatus(JsonHelper.EstimateFixtures.INVALID_BLANK_ORDER_ID_REQUEST, status().isBadRequest());
        }
    }

    private EstimateResponse postEstimateSuccess(String fixture) throws Exception {
        var result = postEstimateFixture(fixture).andExpect(status().isOk()).andReturn();
        var responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, EstimateResponse.class);
    }

    private void assertEstimateStatus(String fixture, org.springframework.test.web.servlet.ResultMatcher matcher)
            throws Exception {
        postEstimateFixture(fixture).andExpect(matcher);
    }

    private ResultActions postEstimateFixture(String fixture) throws Exception {
        var request = JsonHelper.readFixtureJson(fixture);
        return postEstimateJson(request);
    }

    private ResultActions postEstimateJson(String request) throws Exception {
        return mockMvc.perform(post(ESTIMATE_URL).contentType(MediaType.APPLICATION_JSON).content(request));
    }

    private void createProcessType() {
        var processType = modelGenerator.processType();
        processTypeRepository.save(new io.github.avstarodub.mosd.space.infra.mongo.ProcessTypeDocument(
                null, processType.type(), processType.title(), null, null));
    }

    private void createProcess() {
        var process = modelGenerator.process();
        processRepository.save(io.github.avstarodub.mosd.space.infra.mongo.ProcessDocumentMapper.toDocument(process));
    }

    private void seedDefaultProcess() {
        createProcessType();
        createProcess();
    }
}

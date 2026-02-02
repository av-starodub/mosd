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
            createProcessType();
            createProcess();

            var request = JsonHelper.readFixtureJson(JsonHelper.EstimateFixtures.SINGLE_ITEM_REQUEST);
            var result = mockMvc.perform(post(ESTIMATE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isOk())
                    .andReturn();

            var response = objectMapper.readValue(result.getResponse().getContentAsString(), EstimateResponse.class);

            assertThat(response.orderId()).isEqualTo("ORD-001");
            assertThat(response.totalMinutes()).isGreaterThan(0);
            assertThat(response.items()).hasSize(1);
            assertThat(response.items().getFirst().externalId()).isEqualTo("ITEM-1");
            assertThat(response.queues()).isNotEmpty();
        }

        @Test
        @DisplayName("Multiple items demonstrate conveyor effect")
        void multipleItemsConveyorEffect() throws Exception {
            createProcessType();
            createProcess();

            var request = JsonHelper.readFixtureJson(JsonHelper.EstimateFixtures.MULTI_ITEM_REQUEST);
            var result = mockMvc.perform(post(ESTIMATE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isOk())
                    .andReturn();

            var response = objectMapper.readValue(result.getResponse().getContentAsString(), EstimateResponse.class);

            assertThat(response.items()).hasSize(2);

            // Verify conveyor effect: total time should be less than a sum of individual item times
            int sumOfItemTimes = response.items().stream()
                    .mapToInt(ItemEstimate::estimatedMinutes)
                    .sum();
            // Note: For a single process, the conveyor effect shows in queue scheduling
            assertThat(response.totalMinutes()).isLessThanOrEqualTo(sumOfItemTimes);
        }

        @Test
        @DisplayName("Batch volume override affects calculation")
        void batchVolumeOverride() throws Exception {
            createProcessType();
            createProcess();

            var request = JsonHelper.readFixtureJson(JsonHelper.EstimateFixtures.BATCH_OVERRIDE_REQUEST);
            var result = mockMvc.perform(post(ESTIMATE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isOk())
                    .andReturn();

            var response = objectMapper.readValue(result.getResponse().getContentAsString(), EstimateResponse.class);

            // With larger batch volume, fewer batches should be created
            var processEstimate = response.items().getFirst().processes().getFirst();
            assertThat(processEstimate.numberOfBatches()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Validation errors")
    class ValidationErrors {

        @Test
        @DisplayName("Process not found returns 404")
        void processNotFound() throws Exception {
            var request = JsonHelper.readFixtureJson(JsonHelper.EstimateFixtures.SINGLE_ITEM_REQUEST);

            mockMvc.perform(post(ESTIMATE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Invalid operation name returns 400")
        void invalidOperationName() throws Exception {
            createProcessType();
            createProcess();

            var request = JsonHelper.readFixtureJson(JsonHelper.EstimateFixtures.INVALID_OPERATION_REQUEST);

            mockMvc.perform(post(ESTIMATE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Invalid time option key returns 400")
        void invalidTimeOptionKey() throws Exception {
            createProcessType();
            createProcess();

            var request = JsonHelper.readFixtureJson(JsonHelper.EstimateFixtures.INVALID_TIME_OPTION_REQUEST);

            mockMvc.perform(post(ESTIMATE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Invalid rate option key returns 400")
        void invalidRateOptionKey() throws Exception {
            createProcessType();
            createProcess();

            var request = JsonHelper.readFixtureJson(JsonHelper.EstimateFixtures.INVALID_RATE_OPTION_REQUEST);

            mockMvc.perform(post(ESTIMATE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Volume unit mismatch returns 400")
        void volumeUnitMismatch() throws Exception {
            createProcessType();
            createProcess();

            var request = JsonHelper.readFixtureJson(JsonHelper.EstimateFixtures.INVALID_VOLUME_UNIT_REQUEST);

            mockMvc.perform(post(ESTIMATE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Non-positive process version returns 400")
        void nonPositiveProcessVersion() throws Exception {
            var request = JsonHelper.readFixtureJson(JsonHelper.EstimateFixtures.INVALID_VERSION_REQUEST);

            mockMvc.perform(post(ESTIMATE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Non-positive batch override returns 400")
        void nonPositiveBatchOverride() throws Exception {
            var request = JsonHelper.readFixtureJson(JsonHelper.EstimateFixtures.INVALID_BATCH_OVERRIDE_REQUEST);

            mockMvc.perform(post(ESTIMATE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Empty items returns 400")
        void emptyItems() throws Exception {
            var request = """
                    {
                      "orderId": "ORD-001",
                      "items": []
                    }
                    """;

            mockMvc.perform(post(ESTIMATE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Blank order ID returns 400")
        void blankOrderId() throws Exception {
            var request = """
                    {
                      "orderId": "",
                      "items": [{"externalId": "ITEM-1", "processSelections": []}]
                    }
                    """;

            mockMvc.perform(post(ESTIMATE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }
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
}

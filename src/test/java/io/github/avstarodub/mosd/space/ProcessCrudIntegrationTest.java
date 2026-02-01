package io.github.avstarodub.mosd.space;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.avstarodub.mosd.AbstractTestcontainersConfiguration;
import io.github.avstarodub.mosd.IntegrationTest;
import io.github.avstarodub.mosd.space.api.dto.ProcessResponse;
import io.github.avstarodub.mosd.space.api.dto.ProcessTypeCreateRequest;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessDocument;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessDocumentRepository;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessTypeDocument;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessTypeDocumentRepository;
import io.github.avstarodub.mosd.util.JsonHelper;
import io.github.avstarodub.mosd.util.ResultMatcher;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@DisplayName("Integration tests: Space Process CRUD")
class ProcessCrudIntegrationTest extends AbstractTestcontainersConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessDocumentRepository repository;

    @Autowired
    private ProcessTypeDocumentRepository processTypeRepository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        processTypeRepository.deleteAll();
        saveProcessType(JsonHelper.ProcessTypeFixtures.CUT_REQUEST);
        saveProcessType(JsonHelper.ProcessTypeFixtures.WELD_REQUEST);
    }

    @Test
    @DisplayName("Create process persists in repository")
    void createProcessPersists() throws Exception {
        var expected =
                JsonHelper.readFixture(objectMapper, JsonHelper.ProcessFixtures.CREATE_RESPONSE, ProcessResponse.class);

        var actual = createProcess(JsonHelper.ProcessFixtures.CREATE_REQUEST);

        ResultMatcher.assertResponse(actual, expected);
        assertStoredProcess(expected, actual);
    }

    @Test
    @DisplayName("Get latest process by key returns stored data")
    void getLatestProcessByKey() throws Exception {
        createProcess(JsonHelper.ProcessFixtures.CREATE_REQUEST);
        var expected =
                JsonHelper.readFixture(objectMapper, JsonHelper.ProcessFixtures.CREATE_RESPONSE, ProcessResponse.class);

        var actual = getProcess(expected.processKey());

        ResultMatcher.assertResponse(actual, expected);
    }

    @Test
    @DisplayName("Create new process version persists changes")
    void createProcessVersionPersistsChanges() throws Exception {
        createProcess(JsonHelper.ProcessFixtures.CREATE_REQUEST);
        var base =
                JsonHelper.readFixture(objectMapper, JsonHelper.ProcessFixtures.CREATE_RESPONSE, ProcessResponse.class);
        var expected = JsonHelper.readFixture(
                objectMapper, JsonHelper.ProcessFixtures.CREATE_VERSION_RESPONSE, ProcessResponse.class);

        var actual = createVersionFromFixture(base.processKey());

        ResultMatcher.assertResponse(actual, expected);
        assertStoredLatestVersion(expected);
    }

    @Test
    @DisplayName("Delete process removes it from storage")
    void deleteProcess() throws Exception {
        createProcess(JsonHelper.ProcessFixtures.CREATE_REQUEST);
        var expected =
                JsonHelper.readFixture(objectMapper, JsonHelper.ProcessFixtures.CREATE_RESPONSE, ProcessResponse.class);

        deleteProcess(expected.processKey());

        assertProcessNotFound(expected.processKey());
        Assertions.assertThat(repository.findTopByProcessKeyOrderByVersionDesc(expected.processKey()))
                .isEmpty();
    }

    @Test
    @DisplayName("Reject duplicate process key")
    void uniqueProcessKey() throws Exception {
        createProcess(JsonHelper.ProcessFixtures.CREATE_REQUEST);

        postProcess(JsonHelper.ProcessFixtures.CREATE_REQUEST).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reject duplicate operation names within process")
    void uniqueOperationNames() throws Exception {
        postProcess(JsonHelper.ProcessFixtures.INVALID_DUPLICATE_OPERATION).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reject duplicate rate option keys within process")
    void uniqueRateOptionKeys() throws Exception {
        postProcess(JsonHelper.ProcessFixtures.INVALID_DUPLICATE_RATE_OPTION).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reject blank operation name")
    void blankOperationName() throws Exception {
        postProcess(JsonHelper.ProcessFixtures.INVALID_BLANK_OPERATION).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Reject batched mode without batchVolume")
    void batchedModeRequiresBatchVolume() throws Exception {
        postProcess(JsonHelper.ProcessFixtures.INVALID_BATCHED_MISSING_VOLUME).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Reject batchVolume for full-only mode")
    void fullOnlyModeRejectsBatchVolume() throws Exception {
        postProcess(JsonHelper.ProcessFixtures.INVALID_FULL_ONLY_WITH_VOLUME).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("List all processes")
    void getAllProcesses() throws Exception {
        seedProcessesWithVersion();
        var expected = JsonHelper.readFixtureList(
                objectMapper, JsonHelper.ProcessFixtures.LIST_RESPONSE, ProcessResponse[].class);

        var actual = getProcesses();

        ResultMatcher.assertResponseListOrdered(actual, expected);
    }

    @Test
    @DisplayName("Filter latest processes by type")
    void getAllProcessesByType() throws Exception {
        seedProcessesWithVersion();
        var request =
                JsonHelper.readFixture(objectMapper, JsonHelper.ProcessFixtures.CREATE_RESPONSE, ProcessResponse.class);
        var expected = JsonHelper.readFixtureList(
                objectMapper, JsonHelper.ProcessFixtures.LIST_BY_TYPE_RESPONSE, ProcessResponse[].class);

        var actual = getProcessesByType(request.type());

        ResultMatcher.assertResponseListOrdered(actual, expected);
    }

    @Test
    @DisplayName("Reject non-positive version number")
    void rejectNonPositiveVersion() throws Exception {
        mockMvc.perform(get("/api/v1/space/processes/{processKey}/versions/{version}", "PROC-A", 0))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/space/processes/{processKey}/versions/{version}", "PROC-A", -1))
                .andExpect(status().isBadRequest());
    }

    private void seedProcesses() throws Exception {
        createProcess(JsonHelper.ProcessFixtures.CREATE_REQUEST);
        createProcess(JsonHelper.ProcessFixtures.CREATE_REQUEST_B);
    }

    private void seedProcessesWithVersion() throws Exception {
        seedProcesses();
        var base =
                JsonHelper.readFixture(objectMapper, JsonHelper.ProcessFixtures.CREATE_RESPONSE, ProcessResponse.class);
        createVersionFromFixture(base.processKey());
    }

    private void saveProcessType(String fixture) {
        var request = JsonHelper.readFixture(objectMapper, fixture, ProcessTypeCreateRequest.class);
        processTypeRepository.save(new ProcessTypeDocument(null, request.type(), request.title(), null, null));
    }

    private ProcessResponse createProcess(String fixture) throws Exception {
        var result = postProcess(fixture).andExpect(status().isCreated()).andReturn();
        return JsonHelper.readJson(objectMapper, result.getResponse().getContentAsString(), ProcessResponse.class);
    }

    private ProcessResponse createVersionFromFixture(String processKey) throws Exception {
        var content = JsonHelper.readFixtureJson(JsonHelper.ProcessFixtures.CREATE_VERSION_REQUEST);
        var result = mockMvc.perform(post("/api/v1/space/processes/{processKey}/versions", processKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonHelper.readJson(objectMapper, result.getResponse().getContentAsString(), ProcessResponse.class);
    }

    private ProcessResponse getProcess(String processKey) throws Exception {
        var result = mockMvc.perform(get("/api/v1/space/processes/{processKey}", processKey))
                .andExpect(status().isOk())
                .andReturn();
        return JsonHelper.readJson(objectMapper, result.getResponse().getContentAsString(), ProcessResponse.class);
    }

    private List<ProcessResponse> getProcesses() throws Exception {
        var result = mockMvc.perform(get("/api/v1/space/processes"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonHelper.readJsonList(
                objectMapper, result.getResponse().getContentAsString(), ProcessResponse[].class);
    }

    private List<ProcessResponse> getProcessesByType(String type) throws Exception {
        var result = mockMvc.perform(get("/api/v1/space/processes").param("type", type))
                .andExpect(status().isOk())
                .andReturn();
        return JsonHelper.readJsonList(
                objectMapper, result.getResponse().getContentAsString(), ProcessResponse[].class);
    }

    private void deleteProcess(String processKey) throws Exception {
        mockMvc.perform(delete("/api/v1/space/processes/{processKey}", processKey))
                .andExpect(status().isNoContent());
    }

    private void assertProcessNotFound(String processKey) throws Exception {
        mockMvc.perform(get("/api/v1/space/processes/{processKey}", processKey)).andExpect(status().isNotFound());
    }

    private void assertStoredProcess(ProcessResponse expected, ProcessResponse created) {
        var stored = repository.findById(created.id()).orElseThrow();
        Assertions.assertThat(stored)
                .extracting(
                        ProcessDocument::processKey,
                        ProcessDocument::version,
                        ProcessDocument::description,
                        ProcessDocument::volumeUnit,
                        ProcessDocument::batchVolume,
                        ProcessDocument::handoffMode)
                .containsExactly(
                        expected.processKey(),
                        1,
                        expected.description(),
                        expected.volumeUnit(),
                        expected.batchVolume(),
                        expected.handoffMode());
        assertThatJsonEquals(
                JsonHelper.toJsonTree(objectMapper, stored.operations()),
                JsonHelper.toJsonTree(objectMapper, expected.operations()));
        assertThatJsonEquals(
                JsonHelper.toJsonTree(objectMapper, stored.rateOptions()),
                JsonHelper.toJsonTree(objectMapper, expected.rateOptions()));
    }

    private void assertStoredLatestVersion(ProcessResponse expected) {
        var stored = repository
                .findTopByProcessKeyOrderByVersionDesc(expected.processKey())
                .orElseThrow();
        Assertions.assertThat(stored)
                .extracting(
                        ProcessDocument::version,
                        ProcessDocument::description,
                        ProcessDocument::volumeUnit,
                        ProcessDocument::batchVolume,
                        ProcessDocument::handoffMode)
                .containsExactly(
                        expected.version(),
                        expected.description(),
                        expected.volumeUnit(),
                        expected.batchVolume(),
                        expected.handoffMode());
        assertThatJsonEquals(
                JsonHelper.toJsonTree(objectMapper, stored.operations()),
                JsonHelper.toJsonTree(objectMapper, expected.operations()));
        assertThatJsonEquals(
                JsonHelper.toJsonTree(objectMapper, stored.rateOptions()),
                JsonHelper.toJsonTree(objectMapper, expected.rateOptions()));
    }

    private org.springframework.test.web.servlet.ResultActions postProcess(String name) throws Exception {
        var content = JsonHelper.readFixtureJson(name);
        return mockMvc.perform(post("/api/v1/space/processes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
    }

    private void assertThatJsonEquals(JsonNode actual, JsonNode expected) {
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}

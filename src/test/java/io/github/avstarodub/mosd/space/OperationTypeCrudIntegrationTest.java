package io.github.avstarodub.mosd.space;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.avstarodub.mosd.AbstractTestcontainersConfiguration;
import io.github.avstarodub.mosd.IntegrationTest;
import io.github.avstarodub.mosd.space.api.dto.OperationTypeResponse;
import io.github.avstarodub.mosd.space.api.dto.ProcessTypeCreateRequest;
import io.github.avstarodub.mosd.space.infra.mongo.OperationTypeDocumentRepository;
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
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@AutoConfigureMockMvc
@DisplayName("Integration tests: Space OperationType CRUD")
class OperationTypeCrudIntegrationTest extends AbstractTestcontainersConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OperationTypeDocumentRepository repository;

    @Autowired
    private ProcessTypeDocumentRepository processTypeRepository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        processTypeRepository.deleteAll();
        saveProcessType(JsonHelper.ProcessTypeFixtures.CUT_REQUEST);
    }

    @Test
    @DisplayName("Create operation type persists in repository")
    void createOperationTypePersists() throws Exception {
        var expected = JsonHelper.readFixture(
                objectMapper, JsonHelper.OperationTypeFixtures.OP_A_RESPONSE, OperationTypeResponse.class);

        var actual = createOperationType(JsonHelper.OperationTypeFixtures.OP_A_CREATE_REQUEST);

        ResultMatcher.assertResponse(actual, expected);
    }

    @Test
    @DisplayName("Get operation type by type and processType returns stored data")
    void getOperationTypeByTypeAndProcessType() throws Exception {
        createOperationType(JsonHelper.OperationTypeFixtures.OP_A_CREATE_REQUEST);
        var expected = JsonHelper.readFixture(
                objectMapper, JsonHelper.OperationTypeFixtures.OP_A_RESPONSE, OperationTypeResponse.class);

        var actual = getOperationType(expected.type(), expected.processType());

        ResultMatcher.assertResponse(actual, expected);
    }

    @Test
    @DisplayName("List all operation types by processType")
    void getAllOperationTypesByProcessType() throws Exception {
        createOperationType(JsonHelper.OperationTypeFixtures.OP_A_CREATE_REQUEST);
        createOperationType(JsonHelper.OperationTypeFixtures.OP_B_CREATE_REQUEST);
        var expected = JsonHelper.readFixtureList(
                objectMapper, JsonHelper.OperationTypeFixtures.LIST_RESPONSE, OperationTypeResponse[].class);

        var actual = getOperationTypesByProcessType("CUT");

        ResultMatcher.assertResponseListOrdered(actual, expected);
    }

    @Test
    @DisplayName("Reject duplicate type within processType")
    void uniqueTypeWithinProcessType() throws Exception {
        createOperationType(JsonHelper.OperationTypeFixtures.OP_A_CREATE_REQUEST);

        postOperationType(JsonHelper.OperationTypeFixtures.OP_A_CREATE_REQUEST).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reject operation type update via create")
    void rejectUpdateViaCreate() throws Exception {
        createOperationType(JsonHelper.OperationTypeFixtures.OP_A_CREATE_REQUEST);

        var result = postOperationType(JsonHelper.OperationTypeFixtures.INVALID_UPDATE_VIA_CREATE)
                .andExpect(status().isConflict())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("rename/update is forbidden in MVP");
    }

    @Test
    @DisplayName("Reject duplicate time option keys within operation type")
    void uniqueTimeOptionKeys() throws Exception {
        postOperationType(JsonHelper.OperationTypeFixtures.INVALID_DUPLICATE_TIME_OPTION)
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reject non-existent process type")
    void rejectNonExistentProcessType() throws Exception {
        postOperationType(JsonHelper.OperationTypeFixtures.INVALID_MISSING_PROCESS_TYPE)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Return 404 when operation type not found")
    void notFoundWhenOperationTypeDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/space/operation-types/{type}", "NONEXISTENT")
                        .param("processType", "CUT"))
                .andExpect(status().isNotFound());
    }

    private void saveProcessType(String fixture) {
        var request = JsonHelper.readFixture(objectMapper, fixture, ProcessTypeCreateRequest.class);
        processTypeRepository.save(new ProcessTypeDocument(null, request.type(), request.title(), null, null));
    }

    private OperationTypeResponse createOperationType(String fixture) throws Exception {
        var result = postOperationType(fixture).andExpect(status().isCreated()).andReturn();
        return JsonHelper.readJson(
                objectMapper, result.getResponse().getContentAsString(), OperationTypeResponse.class);
    }

    private OperationTypeResponse getOperationType(String type, String processType) throws Exception {
        var result = mockMvc.perform(
                        get("/api/v1/space/operation-types/{type}", type).param("processType", processType))
                .andExpect(status().isOk())
                .andReturn();
        return JsonHelper.readJson(
                objectMapper, result.getResponse().getContentAsString(), OperationTypeResponse.class);
    }

    private List<OperationTypeResponse> getOperationTypesByProcessType(String processType) throws Exception {
        var result = mockMvc.perform(get("/api/v1/space/operation-types").param("processType", processType))
                .andExpect(status().isOk())
                .andReturn();
        return JsonHelper.readJsonList(
                objectMapper, result.getResponse().getContentAsString(), OperationTypeResponse[].class);
    }

    private ResultActions postOperationType(String fixture) throws Exception {
        var content = JsonHelper.readFixtureJson(fixture);
        return mockMvc.perform(post("/api/v1/space/operation-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
    }
}

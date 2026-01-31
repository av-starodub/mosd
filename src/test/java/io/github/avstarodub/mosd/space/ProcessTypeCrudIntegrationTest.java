package io.github.avstarodub.mosd.space;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.avstarodub.mosd.AbstractTestcontainersConfiguration;
import io.github.avstarodub.mosd.IntegrationTest;
import io.github.avstarodub.mosd.space.api.dto.ProcessCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessTypeResponse;
import io.github.avstarodub.mosd.space.api.mapper.ProcessApiMapper;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessDocumentMapper;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessDocumentRepository;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessTypeDocument;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessTypeDocumentRepository;
import io.github.avstarodub.mosd.util.JsonHelper;
import io.github.avstarodub.mosd.util.ResultMatcher;
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
@DisplayName("Integration tests: Space ProcessType CRUD")
class ProcessTypeCrudIntegrationTest extends AbstractTestcontainersConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessTypeDocumentRepository processTypeRepository;

    @Autowired
    private ProcessDocumentRepository processRepository;

    @BeforeEach
    void setUp() {
        processRepository.deleteAll();
        processTypeRepository.deleteAll();
        saveProcessFromFixture();
    }

    @Test
    @DisplayName("Create process type persists in repository")
    void createProcessTypePersists() throws Exception {
        var expected = JsonHelper.readFixture(
                objectMapper, JsonHelper.ProcessTypeFixtures.CUT_RESPONSE, ProcessTypeResponse.class);

        var actual = createProcessType(JsonHelper.ProcessTypeFixtures.CUT_REQUEST);

        ResultMatcher.assertResponse(actual, expected);
        assertStoredProcessType(expected, actual);
    }

    @Test
    @DisplayName("Get process type by key returns stored data")
    void getProcessTypeByType() throws Exception {
        createProcessType(JsonHelper.ProcessTypeFixtures.CUT_REQUEST);
        var expected = JsonHelper.readFixture(
                objectMapper, JsonHelper.ProcessTypeFixtures.CUT_RESPONSE, ProcessTypeResponse.class);

        var result = mockMvc.perform(get("/api/v1/space/process-types/{type}", expected.type()))
                .andExpect(status().isOk())
                .andReturn();

        var actual =
                JsonHelper.readJson(objectMapper, result.getResponse().getContentAsString(), ProcessTypeResponse.class);

        ResultMatcher.assertResponse(actual, expected);
    }

    @Test
    @DisplayName("List all process types")
    void getAllProcessTypes() throws Exception {
        createProcessType(JsonHelper.ProcessTypeFixtures.CUT_REQUEST);
        createProcessType(JsonHelper.ProcessTypeFixtures.WELD_REQUEST);

        var result = mockMvc.perform(get("/api/v1/space/process-types"))
                .andExpect(status().isOk())
                .andReturn();

        var actual = JsonHelper.readJsonList(
                objectMapper, result.getResponse().getContentAsString(), ProcessTypeResponse[].class);

        var expected = JsonHelper.readFixtureList(
                objectMapper, JsonHelper.ProcessTypeFixtures.LIST_RESPONSE, ProcessTypeResponse[].class);

        ResultMatcher.assertResponseListAnyOrder(actual, expected);
    }

    @Test
    @DisplayName("Delete process type removes it from storage")
    void deleteProcessType() throws Exception {
        createProcessType(JsonHelper.ProcessTypeFixtures.WELD_REQUEST);
        var expected = JsonHelper.readFixture(
                objectMapper, JsonHelper.ProcessTypeFixtures.WELD_RESPONSE, ProcessTypeResponse.class);

        mockMvc.perform(delete("/api/v1/space/process-types/{type}", expected.type()))
                .andExpect(status().isNoContent());

        assertThat(processTypeRepository.findByType(expected.type())).isEmpty();
    }

    @Test
    @DisplayName("Reject duplicate process type")
    void duplicateProcessType() throws Exception {
        createProcessType(JsonHelper.ProcessTypeFixtures.CUT_REQUEST);

        postProcessType(JsonHelper.ProcessTypeFixtures.CUT_REQUEST).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reject delete when process type is in use")
    void deleteProcessTypeInUse() throws Exception {
        var created = createProcessType(JsonHelper.ProcessTypeFixtures.CUT_REQUEST);

        mockMvc.perform(delete("/api/v1/space/process-types/{type}", created.type()))
                .andExpect(status().isConflict());
    }

    private ProcessTypeResponse createProcessType(String fixture) throws Exception {
        var result = postProcessType(fixture).andExpect(status().isCreated()).andReturn();
        return JsonHelper.readJson(objectMapper, result.getResponse().getContentAsString(), ProcessTypeResponse.class);
    }

    private void assertStoredProcessType(ProcessTypeResponse expected, ProcessTypeResponse created) {
        var stored = processTypeRepository.findById(created.id()).orElseThrow();
        assertThat(stored)
                .extracting(ProcessTypeDocument::type, ProcessTypeDocument::title)
                .containsExactly(expected.type(), expected.title());
        assertThat(stored.createdAt()).isNotNull();
        assertThat(stored.updatedAt()).isNotNull();
    }

    private void saveProcessFromFixture() {
        var request = JsonHelper.readFixture(
                objectMapper, JsonHelper.ProcessFixtures.CREATE_REQUEST, ProcessCreateRequest.class);
        var domain = ProcessApiMapper.withVersion(ProcessApiMapper.toDomain(request), 1);
        var document = ProcessDocumentMapper.toDocument(domain);
        processRepository.save(document);
    }

    private ResultActions postProcessType(String name) throws Exception {
        var content = JsonHelper.readFixtureJson(name);
        return mockMvc.perform(post("/api/v1/space/process-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
    }
}

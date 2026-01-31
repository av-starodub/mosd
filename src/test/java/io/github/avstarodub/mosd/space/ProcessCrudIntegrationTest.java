package io.github.avstarodub.mosd.space;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.avstarodub.mosd.AbstractTestcontainersConfiguration;
import io.github.avstarodub.mosd.IntegrationTest;
import io.github.avstarodub.mosd.space.api.dto.OperationRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessNewVersionRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessResponse;
import io.github.avstarodub.mosd.space.api.dto.RateResponse;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessDocument;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessDocumentRepository;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessTypeDocument;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessTypeDocumentRepository;
import io.github.avstarodub.mosd.space.infra.mongo.RateDocument;
import io.github.avstarodub.mosd.space.testdata.ModelGenerator;
import java.util.List;
import org.instancio.Instancio;
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

    @Autowired
    private ModelGenerator modelGenerator;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        processTypeRepository.deleteAll();
        processTypeRepository.save(new ProcessTypeDocument(
                null, modelGenerator.getProcessType(), modelGenerator.getProcessTypeTitle(), null, null));
    }

    @Test
    @DisplayName("Create process persists in repository")
    void createProcessPersists() throws Exception {
        var created = createProcess(modelGenerator.getProcessKeyA(), modelGenerator.getProcessDescriptionA());

        var stored = repository.findById(created.id()).orElseThrow();

        assertThat(stored)
                .extracting(
                        ProcessDocument::processKey,
                        ProcessDocument::version,
                        ProcessDocument::description,
                        ProcessDocument::rate)
                .containsExactly(
                        modelGenerator.getProcessKeyA(),
                        1,
                        modelGenerator.getProcessDescriptionA(),
                        modelGenerator.getRateDocumentDefault());
        assertThat(stored.createdAt()).isNotNull();
        assertThat(stored.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Get latest process by key returns stored data")
    void getLatestProcessByKey() throws Exception {
        createProcess(modelGenerator.getProcessKeyA(), modelGenerator.getProcessDescriptionA());

        var fetched = getProcess(modelGenerator.getProcessKeyA());

        assertThat(fetched)
                .extracting(ProcessResponse::processKey, ProcessResponse::version, ProcessResponse::description)
                .containsExactly(modelGenerator.getProcessKeyA(), 1, modelGenerator.getProcessDescriptionA());
    }

    @Test
    @DisplayName("Create new process version persists changes")
    void createProcessVersionPersistsChanges() throws Exception {
        createProcess(modelGenerator.getProcessKeyA(), modelGenerator.getProcessDescriptionA());
        var update = updatedVersionRequest();
        var updated = createVersion(modelGenerator.getProcessKeyA(), update);

        assertUpdatedVersionResponse(updated);
        assertStoredLatestVersion(updated, new RateDocument(2, 1, modelGenerator.getUnit()));
    }

    @Test
    @DisplayName("Delete process removes it from storage")
    void deleteProcess() throws Exception {
        createProcess(modelGenerator.getProcessKeyA(), modelGenerator.getProcessDescriptionA());

        mockMvc.perform(delete("/api/v1/space/processes/{processKey}", modelGenerator.getProcessKeyA()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/space/processes/{processKey}", modelGenerator.getProcessKeyA()))
                .andExpect(status().isNotFound());
        assertThat(repository.findTopByProcessKeyOrderByVersionDesc(modelGenerator.getProcessKeyA()))
                .isEmpty();
    }

    @Test
    @DisplayName("Reject duplicate process key")
    void uniqueProcessKey() throws Exception {
        createProcess(modelGenerator.getProcessKeyA(), modelGenerator.getProcessDescriptionA());

        mockMvc.perform(post("/api/v1/space/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest(
                                modelGenerator.getProcessKeyA(),
                                modelGenerator.getProcessDescriptionA(),
                                modelGenerator.getProcessType()))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reject duplicate operation names within process")
    void uniqueOperationNames() throws Exception {
        var invalid = Instancio.of(modelGenerator.getProcessCreateRequestModel())
                .set(field(ProcessCreateRequest::processKey), modelGenerator.getProcessKeyA())
                .set(field(ProcessCreateRequest::description), modelGenerator.getProcessDescriptionA())
                .set(
                        field(ProcessCreateRequest::operations),
                        List.of(
                                new OperationRequest(modelGenerator.getOperation1(), 5),
                                new OperationRequest(modelGenerator.getOperation1(), 7)))
                .set(field(ProcessCreateRequest::rate), modelGenerator.getRateOneToOne())
                .create();

        mockMvc.perform(post("/api/v1/space/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reject blank operation name")
    void blankOperationName() throws Exception {
        var invalid = Instancio.of(modelGenerator.getProcessCreateRequestModel())
                .set(field(ProcessCreateRequest::processKey), modelGenerator.getProcessKeyA())
                .set(field(ProcessCreateRequest::description), modelGenerator.getProcessDescriptionA())
                .set(field(ProcessCreateRequest::operations), List.of(new OperationRequest(" ", 5)))
                .set(field(ProcessCreateRequest::rate), modelGenerator.getRateOneToOne())
                .create();

        mockMvc.perform(post("/api/v1/space/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("List all processes")
    void getAllProcesses() throws Exception {
        createProcess(modelGenerator.getProcessKeyA(), modelGenerator.getProcessDescriptionA());
        createProcess(modelGenerator.getProcessKeyB(), modelGenerator.getProcessDescriptionB());
        createVersionForSetup(modelGenerator.getProcessKeyA());

        var result = mockMvc.perform(get("/api/v1/space/processes"))
                .andExpect(status().isOk())
                .andReturn();

        var processes = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<List<ProcessResponse>>() {});

        assertThat(processes).hasSize(2);
        var processA = processes.stream()
                .filter(process -> process.processKey().equals(modelGenerator.getProcessKeyA()))
                .findFirst()
                .orElseThrow();
        assertThat(processA.version()).isEqualTo(2);
    }

    @Test
    @DisplayName("Filter latest processes by type")
    void getAllProcessesByType() throws Exception {
        createProcessType(modelGenerator.getProcessTypeB(), modelGenerator.getProcessTypeTitleB());
        createProcess(
                modelGenerator.getProcessKeyA(),
                modelGenerator.getProcessDescriptionA(),
                modelGenerator.getProcessType());
        createProcess(
                modelGenerator.getProcessKeyB(),
                modelGenerator.getProcessDescriptionB(),
                modelGenerator.getProcessTypeB());

        var processes = getProcessesByType(modelGenerator.getProcessType());
        assertThat(processes).hasSize(1);
        assertThat(processes.getFirst().processKey()).isEqualTo(modelGenerator.getProcessKeyA());
    }

    @Test
    @DisplayName("Sort and paginate latest processes")
    void sortAndPaginateProcesses() throws Exception {
        createProcess(modelGenerator.getProcessKeyA(), modelGenerator.getProcessDescriptionA());
        createProcess(modelGenerator.getProcessKeyB(), modelGenerator.getProcessDescriptionB());

        var processes = getProcessesPage();

        assertThat(processes).hasSize(1);
        assertThat(processes.getFirst().processKey()).isEqualTo(modelGenerator.getProcessKeyB());
    }

    private ProcessResponse createProcess(String processKey, String description) throws Exception {
        return createProcess(processKey, description, modelGenerator.getProcessType());
    }

    private ProcessResponse createProcess(String processKey, String description, String type) throws Exception {
        var result = mockMvc.perform(post("/api/v1/space/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest(processKey, description, type))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), ProcessResponse.class);
    }

    private void createVersionForSetup(String processKey) throws Exception {
        createVersion(processKey, sampleVersionRequest());
    }

    private ProcessResponse createVersion(String processKey, ProcessNewVersionRequest request) throws Exception {
        var result = mockMvc.perform(post("/api/v1/space/processes/{processKey}/versions", processKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), ProcessResponse.class);
    }

    private ProcessResponse getProcess(String processKey) throws Exception {
        var result = mockMvc.perform(get("/api/v1/space/processes/{processKey}", processKey))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), ProcessResponse.class);
    }

    private ProcessCreateRequest sampleCreateRequest(String processKey, String description, String type) {
        return Instancio.of(modelGenerator.getProcessCreateRequestModel())
                .set(field(ProcessCreateRequest::processKey), processKey)
                .set(field(ProcessCreateRequest::type), type)
                .set(field(ProcessCreateRequest::description), description)
                .create();
    }

    private void createProcessType(String type, String title) {
        processTypeRepository.save(new ProcessTypeDocument(null, type, title, null, null));
    }

    private List<ProcessResponse> getProcessesByType(String type) throws Exception {
        var result = mockMvc.perform(get("/api/v1/space/processes").param("type", type))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
    }

    private List<ProcessResponse> getProcessesPage() throws Exception {
        var result = mockMvc.perform(get("/api/v1/space/processes")
                        .param("sortBy", "processKey")
                        .param("sortDir", "desc")
                        .param("page", String.valueOf(0))
                        .param("size", String.valueOf(1)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
    }

    private ProcessNewVersionRequest sampleVersionRequest() {
        return Instancio.of(modelGenerator.getProcessNewVersionRequestModel()).create();
    }

    private ProcessNewVersionRequest updatedVersionRequest() {
        return Instancio.of(modelGenerator.getProcessNewVersionRequestModel())
                .set(field(ProcessNewVersionRequest::description), modelGenerator.getProcessDescriptionB())
                .set(
                        field(ProcessNewVersionRequest::operations),
                        List.of(new OperationRequest(modelGenerator.getOperationX(), 7)))
                .set(field(ProcessNewVersionRequest::rate), modelGenerator.getRateUpdate())
                .create();
    }

    private void assertUpdatedVersionResponse(ProcessResponse response) {
        assertThat(response)
                .extracting(
                        ProcessResponse::processKey,
                        ProcessResponse::version,
                        ProcessResponse::description,
                        ProcessResponse::rate)
                .containsExactly(
                        modelGenerator.getProcessKeyA(),
                        2,
                        modelGenerator.getProcessDescriptionB(),
                        new RateResponse(2, 1, modelGenerator.getUnit()));
    }

    private void assertStoredLatestVersion(ProcessResponse response, RateDocument rate) {
        var stored = repository
                .findTopByProcessKeyOrderByVersionDesc(response.processKey())
                .orElseThrow();
        assertThat(stored)
                .extracting(ProcessDocument::version, ProcessDocument::description, ProcessDocument::rate)
                .containsExactly(response.version(), response.description(), rate);
    }
}

package io.github.avstarodub.mosd.space;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.avstarodub.mosd.AbstractTestcontainersConfiguration;
import io.github.avstarodub.mosd.IntegrationTest;
import io.github.avstarodub.mosd.space.api.dto.OperationRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessResponse;
import io.github.avstarodub.mosd.space.api.dto.RateResponse;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessDocument;
import io.github.avstarodub.mosd.space.infra.mongo.ProcessDocumentRepository;
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
    private ModelGenerator modelGenerator;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Create process persists in repository")
    void createProcessPersists() throws Exception {
        var created = createProcess(modelGenerator.getProcessTitleA());

        var stored = repository.findById(created.id()).orElseThrow();

        assertThat(stored)
                .extracting(ProcessDocument::title, ProcessDocument::rate)
                .containsExactly(modelGenerator.getProcessTitleA(), modelGenerator.getRateDocumentDefault());
        assertThat(stored.createdAt()).isNotNull();
        assertThat(stored.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Get process by id returns stored data")
    void getProcessById() throws Exception {
        var stored = repository.save(sampleDocument(modelGenerator.getProcessTitleA()));

        var fetched = getProcess(stored.id());

        assertThat(fetched)
                .extracting(ProcessResponse::id, ProcessResponse::title, ProcessResponse::rate)
                .containsExactly(
                        stored.id(),
                        modelGenerator.getProcessTitleA(),
                        new RateResponse(3, 2, modelGenerator.getUnit()));
    }

    @Test
    @DisplayName("Update process persists changes")
    void updateProcessPersistsChanges() throws Exception {
        var created = createProcess(modelGenerator.getProcessTitleA());
        var update = Instancio.of(modelGenerator.getProcessRequestModel())
                .set(field(ProcessRequest::title), modelGenerator.getProcessTitleB())
                .set(
                        field(ProcessRequest::operations),
                        List.of(new OperationRequest(modelGenerator.getOperationX(), 7)))
                .set(field(ProcessRequest::rate), modelGenerator.getRateUpdate())
                .create();

        var result = mockMvc.perform(put("/api/v1/space/processes/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andReturn();
        var updated = objectMapper.readValue(result.getResponse().getContentAsString(), ProcessResponse.class);
        assertThat(updated)
                .extracting(ProcessResponse::title, ProcessResponse::rate)
                .containsExactly(modelGenerator.getProcessTitleB(), new RateResponse(2, 1, modelGenerator.getUnit()));
        assertThat(updated.updatedAt()).isAfterOrEqualTo(created.updatedAt());
        var stored = repository.findById(created.id()).orElseThrow();
        assertThat(stored)
                .extracting(ProcessDocument::title, ProcessDocument::rate)
                .containsExactly(modelGenerator.getProcessTitleB(), new RateDocument(2, 1, modelGenerator.getUnit()));
    }

    @Test
    @DisplayName("Delete process removes it from storage")
    void deleteProcess() throws Exception {
        var created = createProcess(modelGenerator.getProcessTitleA());

        mockMvc.perform(delete("/api/v1/space/processes/{id}", created.id())).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/space/processes/{id}", created.id())).andExpect(status().isNotFound());
        assertThat(repository.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("Reject duplicate process title")
    void uniqueProcessTitle() throws Exception {
        createProcess(modelGenerator.getProcessTitleA());

        mockMvc.perform(post("/api/v1/space/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest(modelGenerator.getProcessTitleA()))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reject duplicate operation names within process")
    void uniqueOperationNames() throws Exception {
        var invalid = Instancio.of(modelGenerator.getProcessRequestModel())
                .set(field(ProcessRequest::title), modelGenerator.getProcessTitleA())
                .set(
                        field(ProcessRequest::operations),
                        List.of(
                                new OperationRequest(modelGenerator.getOperation1(), 5),
                                new OperationRequest(modelGenerator.getOperation1(), 7)))
                .set(field(ProcessRequest::rate), modelGenerator.getRateOneToOne())
                .create();

        mockMvc.perform(post("/api/v1/space/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reject blank operation name")
    void blankOperationName() throws Exception {
        var invalid = Instancio.of(modelGenerator.getProcessRequestModel())
                .set(field(ProcessRequest::title), modelGenerator.getProcessTitleA())
                .set(field(ProcessRequest::operations), List.of(new OperationRequest(" ", 5)))
                .set(field(ProcessRequest::rate), modelGenerator.getRateOneToOne())
                .create();

        mockMvc.perform(post("/api/v1/space/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("List all processes")
    void getAllProcesses() throws Exception {
        createProcess(modelGenerator.getProcessTitleA());
        createProcess(modelGenerator.getProcessTitleB());

        var result = mockMvc.perform(get("/api/v1/space/processes"))
                .andExpect(status().isOk())
                .andReturn();

        var processes = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<List<ProcessResponse>>() {});

        assertThat(processes).hasSize(2);
    }

    private ProcessResponse createProcess(String title) throws Exception {
        var result = mockMvc.perform(post("/api/v1/space/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest(title))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), ProcessResponse.class);
    }

    private ProcessResponse getProcess(String id) throws Exception {
        var result = mockMvc.perform(get("/api/v1/space/processes/{id}", id))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), ProcessResponse.class);
    }

    private ProcessRequest sampleRequest(String title) {
        return Instancio.of(modelGenerator.getProcessRequestModel())
                .set(field(ProcessRequest::title), title)
                .create();
    }

    private ProcessDocument sampleDocument(String title) {
        return Instancio.of(modelGenerator.getProcessDocumentModel())
                .set(field(ProcessDocument::title), title)
                .create();
    }
}

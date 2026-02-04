package io.github.avstarodub.mosd.space;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.avstarodub.mosd.AbstractTestcontainersConfiguration;
import io.github.avstarodub.mosd.IntegrationTest;
import io.github.avstarodub.mosd.space.api.dto.ProcessCreateRequest;
import io.github.avstarodub.mosd.space.api.dto.ProcessTypeResponse;
import io.github.avstarodub.mosd.space.api.mapper.ProcessApiMapper;
import io.github.avstarodub.mosd.space.api.paths.SpaceApiPaths;
import io.github.avstarodub.mosd.space.infra.mongo.process.ProcessDocumentMapper;
import io.github.avstarodub.mosd.space.infra.mongo.process.ProcessDocumentRepository;
import io.github.avstarodub.mosd.space.infra.mongo.processtype.ProcessTypeDocumentRepository;
import io.github.avstarodub.mosd.util.JsonHelper;
import io.github.avstarodub.mosd.util.ResultMatcher;
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

    @Nested
    @DisplayName("Successful requests")
    class SuccessfulRequests {

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

            var path = SpaceApiPaths.ProcessTypes.BASE + SpaceApiPaths.ProcessTypes.BY_TYPE;
            var result = mockMvc.perform(get(path, expected.type()))
                    .andExpect(status().isOk())
                    .andReturn();
            var responseJson = result.getResponse().getContentAsString();
            var actual = JsonHelper.readJson(objectMapper, responseJson, ProcessTypeResponse.class);

            ResultMatcher.assertResponse(actual, expected);
        }

        @Test
        @DisplayName("List all process types")
        void getAllProcessTypes() throws Exception {
            createProcessType(JsonHelper.ProcessTypeFixtures.CUT_REQUEST);
            createProcessType(JsonHelper.ProcessTypeFixtures.WELD_REQUEST);
            var expected = JsonHelper.readFixtureList(
                    objectMapper, JsonHelper.ProcessTypeFixtures.LIST_RESPONSE, ProcessTypeResponse[].class);

            var result = mockMvc.perform(get(SpaceApiPaths.ProcessTypes.BASE))
                    .andExpect(status().isOk())
                    .andReturn();
            var responseJson = result.getResponse().getContentAsString();
            var actual = JsonHelper.readJsonList(objectMapper, responseJson, ProcessTypeResponse[].class);

            ResultMatcher.assertResponseListAnyOrder(actual, expected);
        }
    }

    @Nested
    @DisplayName("Validation errors")
    class ValidationErrors {

        @Test
        @DisplayName("Reject duplicate process type")
        void duplicateProcessType() throws Exception {
            createProcessType(JsonHelper.ProcessTypeFixtures.CUT_REQUEST);

            postProcessType(JsonHelper.ProcessTypeFixtures.CUT_REQUEST).andExpect(status().isConflict());
        }
    }

    private ProcessTypeResponse createProcessType(String fixture) throws Exception {
        var result = postProcessType(fixture).andExpect(status().isCreated()).andReturn();
        return JsonHelper.readJson(objectMapper, result.getResponse().getContentAsString(), ProcessTypeResponse.class);
    }

    private void assertStoredProcessType(ProcessTypeResponse expected, ProcessTypeResponse created) {
        var stored = processTypeRepository.findById(created.id()).orElseThrow();

        var actualType = stored.type();
        var expectedType = expected.type();
        assertThat(actualType).isEqualTo(expectedType);

        var actualTitle = stored.title();
        var expectedTitle = expected.title();
        assertThat(actualTitle).isEqualTo(expectedTitle);

        var actualCreatedAt = stored.createdAt();
        assertThat(actualCreatedAt).isNotNull();

        var actualUpdatedAt = stored.updatedAt();
        assertThat(actualUpdatedAt).isNotNull();
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
        return mockMvc.perform(post(SpaceApiPaths.ProcessTypes.BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
    }
}

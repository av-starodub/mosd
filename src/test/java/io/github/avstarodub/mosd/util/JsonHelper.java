package io.github.avstarodub.mosd.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonHelper {

    @UtilityClass
    public static class ProcessFixtures {

        public static final String BASE = "fixtures/space/processes/";

        public static final String CREATE_REQUEST = BASE + "create-request.json";

        public static final String CREATE_REQUEST_B = BASE + "create-request-b.json";

        public static final String CREATE_RESPONSE = BASE + "create-response.json";

        public static final String CREATE_VERSION_REQUEST = BASE + "create-version-request.json";

        public static final String CREATE_VERSION_RESPONSE = BASE + "create-version-response.json";

        public static final String LIST_RESPONSE = BASE + "list-response.json";

        public static final String LIST_BY_TYPE_RESPONSE = BASE + "list-by-type-response.json";

        public static final String INVALID_DUPLICATE_OPERATION = BASE + "invalid-duplicate-operation.json";

        public static final String INVALID_DUPLICATE_RATE_OPTION = BASE + "invalid-duplicate-rate-option.json";

        public static final String INVALID_BLANK_OPERATION = BASE + "invalid-blank-operation.json";

        public static final String INVALID_BATCHED_MISSING_VOLUME = BASE + "invalid-batched-missing-volume.json";

        public static final String INVALID_FULL_ONLY_WITH_VOLUME = BASE + "invalid-full-only-with-volume.json";

        public static final String INVALID_DUPLICATE_TIME_OPTION = BASE + "invalid-duplicate-time-option.json";
    }

    @UtilityClass
    public static class ProcessTypeFixtures {

        public static final String BASE = "fixtures/space/process-types/";

        public static final String CUT_REQUEST = BASE + "cut.json";

        public static final String WELD_REQUEST = BASE + "weld.json";

        public static final String CUT_RESPONSE = BASE + "cut-response.json";

        public static final String WELD_RESPONSE = BASE + "weld-response.json";

        public static final String LIST_RESPONSE = BASE + "list-response.json";
    }

    @UtilityClass
    public static class OperationTypeFixtures {

        public static final String BASE = "fixtures/space/operation-types/";

        public static final String OP_A_CREATE_REQUEST = BASE + "op-a-create-request.json";

        public static final String OP_A_RESPONSE = BASE + "op-a-response.json";

        public static final String OP_B_CREATE_REQUEST = BASE + "op-b-create-request.json";

        public static final String OP_B_RESPONSE = BASE + "op-b-response.json";

        public static final String LIST_RESPONSE = BASE + "list-response.json";

        public static final String INVALID_DUPLICATE_TIME_OPTION = BASE + "invalid-duplicate-time-option.json";

        public static final String INVALID_MISSING_PROCESS_TYPE = BASE + "invalid-missing-process-type.json";

        public static final String INVALID_UPDATE_VIA_CREATE = BASE + "invalid-update-via-create.json";
    }

    public static String readFixtureJson(String path) {
        try (var input = JsonHelper.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("Fixture not found: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read fixture: " + path, ex);
        }
    }

    public static <T> T readFixture(ObjectMapper objectMapper, String path, Class<T> type) {
        try {
            return objectMapper.readValue(readFixtureJson(path), type);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse fixture: " + path, ex);
        }
    }

    public static <T> T readJson(ObjectMapper objectMapper, String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse json", ex);
        }
    }

    public static <T> List<T> readFixtureList(ObjectMapper objectMapper, String path, Class<T[]> arrayType) {
        var array = readFixture(objectMapper, path, arrayType);
        return Arrays.asList(array);
    }

    public static <T> List<T> readJsonList(ObjectMapper objectMapper, String json, Class<T[]> arrayType) {
        var array = readJson(objectMapper, json, arrayType);
        return Arrays.asList(array);
    }

    public static JsonNode toJsonTree(ObjectMapper objectMapper, Object value) {
        return objectMapper.valueToTree(value);
    }
}

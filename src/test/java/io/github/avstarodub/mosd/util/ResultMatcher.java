package io.github.avstarodub.mosd.util;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.assertj.core.api.Assertions;

@UtilityClass
public class ResultMatcher {

    private static final String[] DYNAMIC_FIELDS = {"id", "createdAt", "updatedAt"};

    public static <T> void assertResponse(T actual, T expected) {
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields(DYNAMIC_FIELDS)
                .isEqualTo(expected);
        assertDynamicFields(actual);
    }

    public static <T> void assertResponseListOrdered(List<T> actual, List<T> expected) {
        Assertions.assertThat(actual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(DYNAMIC_FIELDS)
                .containsExactlyElementsOf(expected);
        actual.forEach(ResultMatcher::assertDynamicFields);
    }

    public static <T> void assertResponseListAnyOrder(List<T> actual, List<T> expected) {
        Assertions.assertThat(actual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(DYNAMIC_FIELDS)
                .containsExactlyInAnyOrderElementsOf(expected);
        actual.forEach(ResultMatcher::assertDynamicFields);
    }

    private static void assertDynamicFields(Object actual) {
        var id = invoke(actual, "id");
        if (id instanceof String idValue) {
            Assertions.assertThat(idValue).isNotBlank();
        } else {
            Assertions.assertThat(id).isNotNull();
        }
        Assertions.assertThat(invoke(actual, "createdAt")).isNotNull();
        Assertions.assertThat(invoke(actual, "updatedAt")).isNotNull();
    }

    private static Object invoke(Object target, String methodName) {
        try {
            var method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to read field: " + methodName, ex);
        }
    }
}

package io.github.avstarodub.mosd;

import java.time.Duration;
import java.util.Optional;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests requiring MongoDB.
 * Uses "Singleton" container pattern for efficiency.
 */
@Testcontainers
public abstract class AbstractTestcontainersConfiguration {

    private static final String MONGO_IMAGE = "mongo:8.0";

    protected static final MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse(MONGO_IMAGE))
            .withStartupAttempts(2)
            .withStartupTimeout(Duration.ofMinutes(2))
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("testcontainers.mongo")));

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        var externalUri = Optional.ofNullable(System.getProperty("spring.data.mongodb.uri"))
                .or(() -> Optional.ofNullable(System.getenv("SPRING_DATA_MONGODB_URI")))
                .orElse(null);

        if (externalUri != null && !externalUri.isBlank()) {
            registry.add("spring.data.mongodb.uri", () -> externalUri);
            return;
        }

        try {
            Startables.deepStart(MONGO).join();
        } catch (RuntimeException e) {
            throw new IllegalStateException("""
                            Failed to start MongoDB via Testcontainers.
                            Caused by:""" + rootMessage(e), e);
        }

        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    private static String rootMessage(Throwable t) {
        var cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        var msg = cur.getMessage();
        return (msg == null || msg.isBlank()) ? cur.getClass().getName() : msg;
    }
}

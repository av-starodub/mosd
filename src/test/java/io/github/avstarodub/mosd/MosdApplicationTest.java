package io.github.avstarodub.mosd;

import static org.assertj.core.api.Assertions.assertThat;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

@IntegrationTest
@DisplayName("Integration tests: Spring context + MongoDB (Testcontainers)")
class MosdApplicationTest extends AbstractTestcontainersConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoDatabaseFactory mongoDatabaseFactory;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("MongoDB connection is established")
    void mongoDbIsConnected() {
        var dbName = mongoTemplate.getDb().getName();
        assertThat(dbName).isNotBlank();
    }

    @Test
    @DisplayName("Replica set is up and available (replSetGetStatus in admin)")
    void replicaSetIsConfigured() {
        var adminDb = mongoDatabaseFactory.getMongoDatabase("admin");
        var result = adminDb.runCommand(new Document("replSetGetStatus", 1));

        assertThat(result.getString("set")).isNotBlank();
        assertThat(result.getDouble("ok")).isEqualTo(1.0);
    }
}

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.springframework.boot.gradle.tasks.run.BootRun

// Versions from gradle.properties
val springdocVersion: String by project
val checkstyleVersion: String by project
val testcontainersVersion: String by project
val instancioVersion: String by project
val datafakerVersion: String by project

// CVE-2025-48924 vulnerability in versions < 3.18.0
// https://www.mend.io/vulnerability-database/CVE-2025-48924
val commonsLang3Version: String by project

val mongoTag: String = providers
    .systemProperty("mongo.tag")
    .orElse(providers.gradleProperty("mongo.tag"))
    .orElse(providers.environmentVariable("MONGO_TAG"))
    .get()

plugins {
    java
    checkstyle
    jacoco
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "9.4.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    id("com.diffplug.spotless") version "8.2.1"
    id("org.gradle.test-retry") version "1.6.4"
}

val javaVersion: String by project

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:deprecation"))
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    }
    dependencies {
        dependency("org.apache.commons:commons-lang3:$commonsLang3Version")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.instancio:instancio-junit:$instancioVersion")
    testImplementation("net.datafaker:datafaker:$datafakerVersion")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mongodb")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

springBoot {
    buildInfo()
}

checkstyle {
    toolVersion = checkstyleVersion
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
}

spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat()
        endWithNewline()
        trimTrailingWhitespace()
    }
}

openApi {
    outputDir.set(layout.buildDirectory.dir("openapi").get())
    outputFileName.set("openapi.json")
    apiDocsUrl.set("http://localhost:8080/v3/api-docs")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    if (System.getenv("DOCKER_HOST").isNullOrBlank()) {
        environment("DOCKER_HOST", "unix:///var/run/docker.sock")
    }
    systemProperty("mongo.tag", mongoTag)
    retry {
        maxRetries.set(1)
        failOnPassedAfterRetry.set(false)
    }
    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.SKIPPED,
            TestLogEvent.PASSED
        )
        showExceptions = true
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = false
    }
    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("integration")
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Runs integration tests (JUnit @Tag(\"integration\"))"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useJUnitPlatform {
        includeTags("integration")
    }
    shouldRunAfter(tasks.named("test"))
}

tasks.named<Checkstyle>("checkstyleMain") {
    source = fileTree("src/main/java")
}

tasks.named<Checkstyle>("checkstyleTest") {
    source = fileTree("src/test/java")
}

tasks.register("verify") {
    group = "verification"
    description = "Runs all verification tasks"
    dependsOn("check", "integrationTest")
}

tasks.named("check") {
    dependsOn("checkstyleMain", "checkstyleTest")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.register<Exec>("composeUp") {
    group = "compose"; description = "Start local Mongo RS"
    commandLine("docker", "compose", "up", "-d", "--wait")
    onlyIf { System.getenv("CI") != "true" && !project.hasProperty("skipCompose") }
}
tasks.register<Exec>("composeDown") {
    group = "compose"; description = "Stop local Mongo RS and remove volumes"
    commandLine("docker", "compose", "down", "-v")
    onlyIf { System.getenv("CI") != "true" && !project.hasProperty("skipCompose") }
}

tasks.register<BootRun>("bootDev") {
    group = "application"; description = "composeUp + bootRun (dev)"
    dependsOn("composeUp")
    systemProperty("spring.profiles.active", "dev")
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("io.github.avstarodub.mosd.MosdApplication")
}

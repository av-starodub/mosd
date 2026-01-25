import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

val springdocVersion by extra("2.8.13")
val commonsLang3Version by extra("3.19.0") // CVE-2025-48924 vulnerability in versions < 3.18.0 https://www.mend.io/vulnerability-database/CVE-2025-48924?utm_source=JetBrains
val checkstyleVersion by extra("12.1.1")
val mongoTag = providers
    .systemProperty("mongo.tag")
    .orElse(providers.gradleProperty("mongo.tag"))
    .orElse(providers.environmentVariable("MONGO_TAG"))
    .getOrElse("8.0")

plugins {
    java
    checkstyle
    jacoco
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "9.0.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    id("com.diffplug.spotless") version "8.0.0"
    id("org.gradle.test-retry") version "1.6.4"
}

group = "io.github.avstarodub"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
    }
    dependencies {
        dependency("org.apache.commons:commons-lang3:$commonsLang3Version")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
    systemProperty("mongo.tag", mongoTag)
    finalizedBy(tasks.jacocoTestReport)
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

tasks.named<Checkstyle>("checkstyleMain") {
    source = fileTree("src/main/java")
}

tasks.named<Checkstyle>("checkstyleTest") {
    source = fileTree("src/test/java")
}

tasks.register("verify") {
    group = "verification"
    description = "Runs all verification tasks"
    dependsOn("check")
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

tasks.register("bootDev") {
    group = "application"; description = "composeUp + bootRun (dev)"
    dependsOn("composeUp", "bootRun")
}

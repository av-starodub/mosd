plugins {
    java
    checkstyle
}

group = "io.github.avstarodub"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

checkstyle {
    toolVersion = "10.12.5"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
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

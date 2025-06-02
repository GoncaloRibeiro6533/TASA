plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.serialization") version "1.9.25"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "pt.isel"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":http-api"))
    implementation(project(":repository"))
    implementation(project(":http-pipeline"))
    implementation(project(":repository-jdbi"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    // for JDBI and Postgres
    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.postgresql:postgresql:42.7.2")

    // for OpenApi docs generation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.bootRun {
    environment("JDBC_DATABASE_URL", "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres")
}

tasks.withType<Test> {
    useJUnitPlatform()
    environment("JDBC_DATABASE_URL", "jdbc:postgresql://localhost:5434/db?user=dbuser&password=changeit")
    dependsOn(":repository-jdbi:dbTestsWait")
    finalizedBy(":repository-jdbi:dbTestsDown")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

task<Copy>("extractUberJar") {
    dependsOn("assemble")
    // opens the JAR containing everything...
    from(zipTree(layout.buildDirectory.file("libs/host-$version.jar").get().toString()))
    // ... into the 'build/dependency' folder
    into("build/dependency")
}

val dockerImageJvm = "tasa-jvm"
val dockerImageNginx = "tasa-nginx"
val dockerImagePostgresTest = "tasa-postgres-test"

task<Exec>("buildImageJvm") {
    dependsOn("extractUberJar")
    commandLine("docker", "build", "-t", dockerImageJvm, "-f", "test-infra/Dockerfile-jvm", ".")
}

task<Exec>("buildImageNginx") {
    commandLine("docker", "build", "-t", dockerImageNginx, "-f", "test-infra/Dockerfile-nginx", ".")
}

task<Exec>("buildImagePostgresTest") {
    commandLine(
        "docker",
        "build",
        "-t",
        dockerImagePostgresTest,
        "-f",
        "test-infra/Dockerfile-postgres-test",
        "../repository-jdbi",
    )
}

task("buildImageAll") {
    dependsOn("buildImageJvm")
    dependsOn("buildImageNginx")
    dependsOn("buildImagePostgresTest")
    // dependsOn("buildImageUbuntu")
}

task<Exec>("startAll") {
    dependsOn("buildImageAll")
    commandLine("docker", "compose", "up", "--force-recreate", "-d")
}

task<Exec>("allDown") {
    commandLine("docker", "compose", "down")
}

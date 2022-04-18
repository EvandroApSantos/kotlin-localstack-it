plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("com.avast.gradle.docker-compose") version "0.15.2"

    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

    implementation(platform("software.amazon.awssdk:bom:2.17.171"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:sqs")

    implementation("com.typesafe:config:1.4.2")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")


    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClass.set("AppKt")
}

val integrationTests = task<Test>("integrationTests") {
    group = "verification"
    include("*IT")
}

tasks {
    integrationTests
    test {
        exclude("*IT")
    }
}

dockerCompose {
    isRequiredBy(integrationTests)
    setProperty("useComposeFiles", listOf("infra/docker-compose.yaml"))
}

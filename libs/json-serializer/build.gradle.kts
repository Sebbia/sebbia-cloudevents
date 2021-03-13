//val kotlinVersion: String by project

//plugins {
//    id("java-library")
//    id("maven-publish")
//    kotlin("jvm")
//}

dependencies {
    api(project(":core"))

//    api("org.jooq", "jooq", "3.12.3")
//    implementation(kotlin("stdlib-jdk8", kotlinVersion))
//    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.4.2")
//    implementation(kotlin("reflect", kotlinVersion))
//    implementation("com.zaxxer", "HikariCP", "3.1.0")
//    implementation("org.flywaydb", "flyway-core", "6.2.0")
//    implementation("com.sebbia", "kotlin-errors", "1.0.0")
//    implementation("com.sebbia", "kotlin-model", "1.0.0")
//    implementation("com.sebbia", "kotlin-logger", "1.0.0")
//    implementation("com.sebbia", "kotlin-scopes", "1.0.0")
//
//    testImplementation(kotlin("test-junit", kotlinVersion))
//    testImplementation("com.h2database", "h2", "1.4.200")
//    testImplementation("ch.qos.logback", "logback-classic", "1.2.3")
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
}

//kotlin.sourceSets["main"].kotlin.srcDirs("src")
//kotlin.sourceSets["test"].kotlin.srcDirs("test")

//sourceSets["test"].resources.srcDirs("testresources")
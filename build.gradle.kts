repositories {
    mavenCentral()
}

plugins {
    id("java-library")
    id("maven-publish")
    kotlin("jvm")
}

val kotlinVersion: String by project

subprojects {
    group = "com.sebbia.cloudevents"
    version = "0.0.1"

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    java {
        withSourcesJar()
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<Jar> {
        archiveBaseName.set(rootProject.name + "-" + archiveBaseName.get())
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = group as String
                artifactId = project.name
                version = version
                from(components["java"])
            }
        }
        repositories {
            maven {
                credentials {
                    username = "maven-upload"
                    password = "maven"
                }
                url = uri("http://hub.sebbia.org/repository/maven-releases/")
            }
        }
    }

    repositories {
        mavenCentral()
        maven(url = "http://hub.sebbia.org/repository/maven-releases/")
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        testImplementation(kotlin("test-junit"))
    }

    kotlin.sourceSets["main"].kotlin.srcDirs("src")
    kotlin.sourceSets["test"].kotlin.srcDirs("test")

    sourceSets["test"].resources.srcDirs("testresources")
}
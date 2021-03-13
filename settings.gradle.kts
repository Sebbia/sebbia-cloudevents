rootProject.name = "sebbia-cloudevents"

listOf(
    "core",
    "json-serializer"
).forEach {
    include(":$it")
    project(":$it").apply {
        projectDir = file("libs/$it")
    }
}

pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion
    }
}


dependencies {
    api(project(":core"))
    implementation("io.nats:java-nats-streaming:2.2.3")
    testImplementation(project(":json-serializer"))
}

import com.sebbia.cloudevents.core.CloudEvent
import com.sebbia.cloudevents.json.CloudEventsJsonSerializer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

suspend fun waitForTrue(timeoutMs: Int = 10000, block: () -> Boolean) {
    val pause = 100
    for (i in 1..timeoutMs / pause) {
        if (block()) return
        delay(100)
    }
    assertTrue(false)
}

class ClientTest {

    private val serializer = CloudEventsJsonSerializer()

    private fun runWithClient(
        clientId: String? = null,
        opts: List<NatsSubscriptionOptions>? = null,
        block: (NatsCloudEventsBusClient) -> Unit
    ) {
        NatsCloudEventsBusClient(
            natsUrl = "nats://localhost:4222",
            clusterId = "some-cluster",
            clientId = clientId ?: "client",
            subscriptionOptions = opts,
            serializer = serializer
        ).use { client ->
            block(client)
        }
    }

    private val baseEvent = CloudEvent(
        specVersion = "1.0",
        type = "event",
        source = "test",
        id = "1"
    )

    @Test
    fun `Send event`() {
        runWithClient { client ->
            runBlocking {
                val subjectName = "subject"
                val collected = mutableListOf<CloudEvent>()
                client.subscribe(subjectName) {
                    collected.add(it)
                }.use {
                    for (i in 1..2) {
                        client.publish(
                            subjectName, listOf(
                                baseEvent.copy(
                                    id = i.toString()
                                )
                            )
                        )
                    }
                    waitForTrue {
                        collected.size == 2
                    }
                }
            }
        }
    }

    @Test
    fun `Send event to durable subscription`() {

        val subjectName = "durable_subject"

        runWithClient(clientId = "sender") { sender ->
            runWithClient(
                clientId = "receiver",
                opts = listOf(
                    NatsSubscriptionOptions(
                        subject = subjectName,
                        durableName = "durable_subject",
                        // first run deliver all available events in que
                        // second run durable pointer used to read events
                        deliverAllAvailable = true
                    )
                )
            ) { receiver ->

                val collected = mutableListOf<CloudEvent>()

                //send events to subject
                for (i in 1..3) {
                    sender.publish(
                        subjectName, listOf(
                            baseEvent.copy(
                                id = i.toString()
                            )
                        )
                    )
                }

                // receive messages from durable subscription
                receiver.subscribe(subjectName) {
                    collected.add(it)
                }.use {
                    runBlocking {
                        waitForTrue {
                            collected.size == 3
                        }
                    }
                }
            }
        }
    }
}

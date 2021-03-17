import com.sebbia.cloudevents.core.CloudEvent
import com.sebbia.cloudevents.core.CloudEventsBusClient
import com.sebbia.cloudevents.core.CloudEventsSubscription
import com.sebbia.cloudevents.json.CloudEventsJsonSerializer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
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

    private fun createClient(
        clientId: String? = null,
        opts: NatsSubscriptionOptions? = null
    ): CloudEventsBusClient =
        NatsCloudEventsBusClient(
            natsUrl = "nats://localhost:4222",
            clusterId = "some-cluster",
            clientId = clientId ?: "client",
            subscriptionOptions = listOfNotNull(opts),
            serializer = serializer
        )

    private fun runWithClient(
        clientId: String? = null,
        opts: NatsSubscriptionOptions? = null,
        block: (CloudEventsBusClient) -> Unit
    ) {
        createClient(
            clientId = clientId,
            opts = opts
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
                opts = NatsSubscriptionOptions(
                    subject = subjectName,
                    durableName = "durable_subject",
                    // first run deliver all available events in que
                    // second run durable pointer used to read events
                    deliverAllAvailable = true
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

    @Test
    fun `Load balanced event processing`() {
        var sender: CloudEventsBusClient? = null
        var receiver1: CloudEventsBusClient? = null
        var receiver2: CloudEventsBusClient? = null
        var subscription1: CloudEventsSubscription? = null
        var subscription2: CloudEventsSubscription? = null
        try {
            val subject = "queued_subject"
            val queueName = "receiver_pool"

            sender = createClient(clientId = "que_sender")
            receiver1 = createClient(
                clientId = "que_receiver1",
                opts = NatsSubscriptionOptions(subject = subject, queueName = queueName)
            )
            receiver2 = createClient(
                clientId = "que_receiver2",
                opts = NatsSubscriptionOptions(subject = subject, queueName = queueName)
            )

            val collected1 = mutableListOf<CloudEvent>()
            val collected2 = mutableListOf<CloudEvent>()

            subscription1 = receiver1.subscribe(subject) {
                collected1.add(it)
            }
            subscription2 = receiver2.subscribe(subject) {
                collected2.add(it)
            }

            for (i in 1..4) {
                sender.publish(subject, listOf(baseEvent.copy(id = i.toString())))
            }

            runBlocking {
                waitForTrue {
                    collected1.size + collected2.size == 4
                }
            }

            assertEquals("1,3", collected1.joinToString(",") { it.id })
            assertEquals("2,4", collected2.joinToString(",") { it.id })

        } finally {
            subscription1?.close()
            subscription2?.close()
            receiver1?.close()
            receiver2?.close()
            sender?.close()
        }
    }
}

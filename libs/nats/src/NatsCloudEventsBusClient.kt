import com.sebbia.cloudevents.core.CloudEvent
import com.sebbia.cloudevents.core.CloudEventsBusClient
import com.sebbia.cloudevents.core.CloudEventsSerializer
import com.sebbia.cloudevents.core.CloudEventsSubscription
import io.nats.streaming.Options
import io.nats.streaming.StreamingConnection
import io.nats.streaming.StreamingConnectionFactory
import io.nats.streaming.SubscriptionOptions

data class NatsSubscriptionOptions(
    val subject: String,
    val durableName: String? = null,
    val deliverAllAvailable: Boolean? = null,
    val maxInFlight: Int? = null,
    val queueName: String? = null // load balancing clients in the same que
)

class NatsCloudEventsBusClient(
    natsUrl: String,
    clusterId: String,
    clientId: String,
    private val subscriptionOptions: List<NatsSubscriptionOptions>? = null,
    private val serializer: CloudEventsSerializer
) : CloudEventsBusClient {

    private val connection: StreamingConnection

    private class SubscriptionImpl(
        val subscription: io.nats.streaming.Subscription
    ) : CloudEventsSubscription {

        override fun close() {
            subscription.close(false)
        }

        override fun unsubscribe() {
            subscription.unsubscribe()
        }
    }

    init {
        val options = Options.Builder().natsUrl(natsUrl).clusterId(clusterId).clientId(clientId).build()
        val factory = StreamingConnectionFactory(options)
        connection = factory.createConnection()
    }

    override fun publish(subject: String, events: List<CloudEvent>) {
        if (serializer.batchingSupported) {
            connection.publish(subject, serializer.serialize(events))
        } else {
            events.forEach {
                connection.publish(subject, serializer.serialize(listOf(it)))
            }
        }
    }

    override fun subscribe(subject: String, handler: (CloudEvent) -> Unit): CloudEventsSubscription {

        val userOptions = subscriptionOptions?.find { it.subject == subject }

        val opts = userOptions?.let { opts ->
            SubscriptionOptions.Builder().apply {
                opts.durableName?.let { durableName(it) }
                opts.deliverAllAvailable?.let { if (it) deliverAllAvailable() }
                opts.maxInFlight?.let { maxInFlight(it) }
            }.build()
        }

        val subscription = connection.subscribe(subject, userOptions?.queueName, { message ->
            val cloudEventMessages = serializer.deserialize(message.data)
            cloudEventMessages.forEach { handler(it) }
            message.ack()
        }, opts)
        return SubscriptionImpl(subscription)
    }

    override fun close() {
        connection.close()
    }
}
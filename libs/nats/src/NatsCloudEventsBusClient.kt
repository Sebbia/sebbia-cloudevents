import com.sebbia.cloudevents.core.CloudEvent
import com.sebbia.cloudevents.core.CloudEventsBusClient
import com.sebbia.cloudevents.core.CloudEventsSerializer
import com.sebbia.cloudevents.core.Subscription
import io.nats.streaming.Options
import io.nats.streaming.StreamingConnection
import io.nats.streaming.StreamingConnectionFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking

class NatsCloudEventsBusClient(
    natsUrl: String,
    clusterId: String,
    clientId: String,
    private val serializer: CloudEventsSerializer
) : CloudEventsBusClient, AutoCloseable {

    private val connection: StreamingConnection

    private class SubscriptionImpl<T>(
        val subscription: io.nats.streaming.Subscription,
        override val flow: Flow<T>
    ) : Subscription<T> {
        override fun close() {
            subscription.close(true)
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

    override fun subscribe(subject: String): Subscription<CloudEvent> {
        val flow = MutableSharedFlow<CloudEvent>()
        val subscription = connection.subscribe(subject) { message ->
            val cloudEventMessages = serializer.deserialize(message.data)
            runBlocking {
                cloudEventMessages.forEach { flow.emit(it) }
            }
            message.ack()
        }
        return SubscriptionImpl(subscription, flow.asSharedFlow())
    }

    override fun close() {
        connection.close()
    }
}
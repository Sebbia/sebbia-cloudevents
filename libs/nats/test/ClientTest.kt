import com.sebbia.cloudevents.json.CloudEventsJsonSerializer
import kotlin.test.Test

class ClientTest {

    val serializer = CloudEventsJsonSerializer()

    @Test
    fun `Send event`() {

        NatsCloudEventsBusClient(
            natsUrl = "nats://localhost:4222",
            clusterId = "cluster",
            clientId = "client",
            serializer = serializer
        ).use { client ->

        }

    }
}
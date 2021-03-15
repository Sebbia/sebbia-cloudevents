import com.sebbia.cloudevents.core.CloudEvent
import com.sebbia.cloudevents.core.CloudEventData
import com.sebbia.cloudevents.core.ContentType
import com.sebbia.cloudevents.json.CloudEventsJsonSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonSerializeTest {

    private val serializer = CloudEventsJsonSerializer()
    private val basicEvent = CloudEvent(
        id = "1",
        type = "test/event",
        source = "test",
        specVersion = "1.0"
    )

    @Test
    fun `Serialize single simple event`() {
        val data = serializer.serialize(
            listOf(basicEvent)
        )
        assertEquals(
            "{\"id\":\"1\",\"specversion\":\"1.0\",\"source\":\"test\",\"type\":\"test/event\"}",
            String(data)
        )
    }

    @Test
    fun `Serialize event with data`() {
        val data = serializer.serialize(
            listOf(
                basicEvent.copy(
                    data = CloudEventData.Str("test")
                )
            )
        )
        assertEquals(
            """{"id":"1","specversion":"1.0","source":"test","type":"test/event","datacontenttype":"text/plain","data":"test"}""",
            String(data)
        )
    }

    @Test
    fun `Serialize event with JSON data`() {
        val data = serializer.serialize(
            listOf(
                basicEvent.copy(
                    data = CloudEventData.ValueTree(
                        mapOf(
                            "first" to mapOf(
                                "key1" to 10, "key2" to 20
                            ),
                            "second" to listOf(1, 2, 3),
                            "third" to "value"
                        )
                    )
                )
            )
        )

        val json =
            """{"id":"1","specversion":"1.0","source":"test","type":"test/event","datacontenttype":"application/json","data":{"first":{"key1":10,"key2":20},"second":[1,2,3],"third":"value"}}"""
        assertEquals(json, String(data))
    }

    @Test
    fun `Serialize ByteArray data`() {
        val data = serializer.serialize(
            listOf(
                basicEvent.copy(
                    data = CloudEventData.Binary("Test".toByteArray())
                )
            )
        )

        val json =
            """{"id":"1","specversion":"1.0","source":"test","type":"test/event","datacontenttype":"application/octet-stream","data":"VGVzdA=="}"""
        assertEquals(json, String(data))
    }

    @Test
    fun `Serialize batched events`() {
        val data = serializer.serialize(
            listOf(
                basicEvent.copy(id = "1"),
                basicEvent.copy(id = "2"),
            )
        )
        val json =
            """[{"id":"1","specversion":"1.0","source":"test","type":"test/event"},{"id":"2","specversion":"1.0","source":"test","type":"test/event"}]"""
        assertEquals(json, String(data))
    }

    @Test
    fun `Serialize additional fields`() {
        val data = serializer.serialize(
            listOf(
                basicEvent.copy(additionalFields = mapOf("field" to "value"))
            )
        )
        val json =
            """{"id":"1","specversion":"1.0","source":"test","type":"test/event","field":"value"}"""
        assertEquals(json, String(data))
    }

    @Test
    fun `Deserialize batched event with base64 data`() {
        val srcEvents = listOf(
            basicEvent.copy(
                id = "1",
                dataContentType = ContentType.Application.OctetStream,
                data = CloudEventData.Binary("Test1".toByteArray())
            ),
            basicEvent.copy(
                id = "2",
                dataContentType = ContentType.Application.OctetStream,
                data = CloudEventData.Binary("Test2".toByteArray())
            )
        )
        val data = serializer.serialize(srcEvents)
        val events = serializer.deserialize(data)
        assertEquals(srcEvents, events)
    }

    @Test
    fun `Deserialize event with JSON data`() {
        val srcEvents = listOf(
            basicEvent.copy(
                id = "1",
                dataContentType = ContentType.Application.Json,
                data = CloudEventData.ValueTree(mapOf("key" to "value"))
            ),
            basicEvent.copy(
                id = "2",
                dataContentType = ContentType.Application.Json,
                data = CloudEventData.ValueTree(listOf(mapOf("key" to "value")))
            )
        )
        val data = serializer.serialize(srcEvents)
        val events = serializer.deserialize(data)
        assertEquals(srcEvents, events)
    }

    @Test
    fun `Deserialize with additional fields`() {
        val srcEvents = listOf(
            basicEvent.copy(additionalFields = mapOf("field" to "value"))
        )
        val data = serializer.serialize(srcEvents)
        val events = serializer.deserialize(data)
        assertEquals(srcEvents, events)
    }

}

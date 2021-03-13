package com.sebbia.cloudevents.json

import com.sebbia.cloudevents.core.CloudEventData
import com.sebbia.cloudevents.core.CloudEventsSerializer

class CloudEventsJsonSerializer : CloudEventsSerializer {
    override fun serialize(event: CloudEventData): ByteArray {
        TODO("Not yet implemented")
    }

    override fun deserialize(data: ByteArray): CloudEventData {
        TODO("Not yet implemented")
    }
}
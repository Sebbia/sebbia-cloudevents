package com.sebbia.cloudevents.core

import kotlinx.coroutines.flow.Flow
import java.time.Instant

sealed class CloudEventData {

    data class Str(val data: String) : CloudEventData()

    data class KeyValue(val data: Map<String, *>) : CloudEventData()

    data class Binary(val data: ByteArray) : CloudEventData() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Binary
            if (!data.contentEquals(other.data)) return false
            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }
}

sealed class CloudEventTracing {

    data class Distributed(
        val traceParent: String,
        val traceState: String? = null
    ) : CloudEventTracing()

    data class Legacy(
        val scope: String
    )

}

data class CloudEvent(
    val specVersion: String,
    val type: String,
    val source: String,
    val id: String,
    val subject: String? = null,
    val time: Instant? = null,
    val dataContentType: String? = null,
    val dataSchema: String? = null,
    val tracing: CloudEventTracing? = null,
    val data: CloudEventData?
)

interface CloudEventsSerializer {
    fun serialize(event: CloudEventData): ByteArray
    fun deserialize(data: ByteArray): CloudEventData
}

interface CloudEventsBusClient {
    fun publish(subject: String, event: CloudEventData)
    fun subscribe(subject: String): Flow<CloudEventData>
}
package com.sebbia.cloudevents.core

interface CloudEventsSerializer {
    val batchingSupported: Boolean
    fun serialize(events: List<CloudEvent>): ByteArray
    fun deserialize(data: ByteArray): List<CloudEvent>
}
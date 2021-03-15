package com.sebbia.cloudevents.core

import kotlinx.coroutines.flow.Flow

interface CloudEventsBusClient {
    fun publish(subject: String, event: CloudEvent)
    fun subscribe(subject: String): Flow<CloudEvent>
}
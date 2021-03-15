package com.sebbia.cloudevents.core

import java.time.Instant

data class CloudEvent(
    val specVersion: String,
    val type: String,
    val source: String,
    val id: String,
    val subject: String? = null,
    val time: Instant? = null,
    val dataContentType: ContentType? = null,
    val dataSchema: String? = null,
    val tracing: CloudEventTracing? = null,
    val additionalFields: Map<String, *>? = null,
    val data: CloudEventData? = null
)
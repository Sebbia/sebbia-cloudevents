package com.sebbia.cloudevents.core

interface CloudEventsBusClient : AutoCloseable {
    fun publish(subject: String, events: List<CloudEvent>)
    fun subscribe(subject: String, handler: (CloudEvent) -> Unit): CloudEventsSubscription
}
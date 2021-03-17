package com.sebbia.cloudevents.core

interface CloudEventsBusClient {
    fun publish(subject: String, events: List<CloudEvent>)
    fun subscribe(subject: String, handler: (CloudEvent) -> Unit): CloudEventsSubscription
}
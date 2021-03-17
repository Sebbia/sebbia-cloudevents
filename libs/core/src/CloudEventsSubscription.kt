package com.sebbia.cloudevents.core

interface CloudEventsSubscription : AutoCloseable {
    fun unsubscribe()
}
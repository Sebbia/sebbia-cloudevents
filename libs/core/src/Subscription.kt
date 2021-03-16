package com.sebbia.cloudevents.core

import kotlinx.coroutines.flow.Flow

interface Subscription<T> : AutoCloseable {
    val flow: Flow<T>
}
package com.sebbia.cloudevents.core

sealed class CloudEventTracing {

    data class Distributed(
        val traceParent: String,
        val traceState: String? = null
    ) : CloudEventTracing()

    data class Legacy(
        val scope: String
    )

}
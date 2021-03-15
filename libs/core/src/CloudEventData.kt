package com.sebbia.cloudevents.core

sealed class CloudEventData {

    data class Str(val data: String) : CloudEventData()

    data class ValueTree(val data: Any) : CloudEventData()

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
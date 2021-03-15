package com.sebbia.cloudevents.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sebbia.cloudevents.core.*
import java.time.Instant

class CloudEventsJsonSerializer : CloudEventsSerializer {
    override val batchingSupported: Boolean = true

    private val objectMapper = jacksonObjectMapper()

    private fun detectContentTypeByEventData(data: CloudEventData) =
        when (data) {
            is CloudEventData.Str -> ContentType.Text.Plain
            is CloudEventData.ValueTree -> ContentType.Application.Json
            is CloudEventData.Binary -> ContentType.Application.OctetStream
        }

    private fun serializeEvent(event: CloudEvent): Map<String, *> =
        (listOfNotNull(
            KnownFields.ID to event.id,
            KnownFields.SPEC_VERSION to event.specVersion,
            KnownFields.SOURCE to event.source,
            KnownFields.TYPE to event.type,
            event.subject?.let { KnownFields.SUBJECT to it },
            event.dataSchema?.let { KnownFields.DATA_SCHEMA to it },
            event.time?.let { KnownFields.TIME to it.toString() },
            (event.dataContentType
                ?: event.data?.let { detectContentTypeByEventData(it) })?.let { KnownFields.DATA_CONTENT_TYPE to it.toString() },
            event.data?.let {
                KnownFields.DATA to when (it) {
                    is CloudEventData.Str -> it.data
                    is CloudEventData.ValueTree -> objectMapper.convertValue<Any>(it.data)
                    is CloudEventData.Binary -> it.data
                }
            },
        ).map { it.first.fieldName to it.second } + (event.additionalFields?.toList() ?: emptyList())).toMap()

    override fun serialize(events: List<CloudEvent>): ByteArray =
        when {
            events.isEmpty() -> ByteArray(0)
            events.size == 1 -> objectMapper.writeValueAsBytes(serializeEvent(events[0]))
            else -> objectMapper.writeValueAsBytes(events.map { serializeEvent(it) })
        }

    private fun detectContentType(data: JsonNode): ContentType {
        if (data.isTextual) {
            val text = data.asText().trim()
            if (text.isNotEmpty() && (text[0] != '{' || text[0] != '[')) {
                return ContentType.Application.Json
            }
            return ContentType.Text.Plain
        }
        if (data.isBinary) {
            return ContentType.Application.OctetStream
        }
        throw IllegalArgumentException("Cannot detect content type for data")
    }

    private fun parseData(data: JsonNode, contentType: ContentType?): CloudEventData =
        when (val finalContentType = contentType ?: detectContentType(data)) {
            ContentType.Text.Plain -> CloudEventData.Str(data.asText())
            ContentType.Application.Json ->
                CloudEventData.ValueTree(
                    when {
                        data.isTextual -> objectMapper.readValue(data.asText())
                        data.isArray || data.isObject -> objectMapper.convertValue(data)
                        else -> throw IllegalArgumentException("Unsupported json node type for data field")
                    }
                )
            ContentType.Application.OctetStream, ContentType.Application.Protobuf ->
                CloudEventData.Binary(data.binaryValue())
            else -> throw IllegalArgumentException("Unsupported content type $finalContentType")
        }

    val knownFields = KnownFields.values().map { it.fieldName }.toSet()

    private fun parseEvent(node: JsonNode): CloudEvent {
        val contentType = node[KnownFields.DATA_CONTENT_TYPE.fieldName]?.asText()?.toContentType()

        val additionalFields = node.fields()
            .asSequence().map {
                if (!knownFields.contains(it.key))
                    it.key to when {
                        it.value.isTextual -> it.value.asText()
                        it.value.isBoolean -> it.value.asBoolean()
                        it.value.isInt -> it.value.asInt()
                        it.value.isFloat -> it.value.asDouble()
                        it.value.isLong -> it.value.asLong()
                        it.value.isBinary -> it.value.binaryValue()
                        else -> throw IllegalArgumentException("Unsupported value type: ${it.value.nodeType}")
                    }
                else
                    null
            }.filterNotNull().toMap()

        return CloudEvent(
            id = node.required(KnownFields.ID.fieldName).asText(),
            specVersion = node.required(KnownFields.SPEC_VERSION.fieldName).asText(),
            source = node.required(KnownFields.SOURCE.fieldName).asText(),
            type = node.required(KnownFields.TYPE.fieldName).asText(),
            subject = node[KnownFields.SUBJECT.fieldName]?.asText(),
            dataSchema = node[KnownFields.DATA_SCHEMA.fieldName]?.asText(),
            time = node[KnownFields.TIME.fieldName]?.asText()?.let { Instant.parse(it) },
            dataContentType = contentType,
            data = node[KnownFields.DATA.fieldName]?.let { parseData(it, contentType) },
            additionalFields = additionalFields
        )
    }

    override fun deserialize(data: ByteArray): List<CloudEvent> {
        val node = objectMapper.readTree(data)
        return when {
            node.isObject -> listOf(parseEvent(node))
            node.isArray -> node.asSequence().map { parseEvent(it) }.toList()
            else -> throw IllegalStateException("Node is not object or array")
        }
    }
}
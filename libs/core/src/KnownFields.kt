package com.sebbia.cloudevents.core

enum class KnownFields(val fieldName: String, val required: Boolean) {
    ID("id", true),
    SPEC_VERSION("specversion", true),
    SOURCE("source", true),
    TYPE("type", true),
    SUBJECT("subject", false),
    DATA_SCHEMA("dataschema", false),
    TIME("time", false),
    DATA_CONTENT_TYPE("datacontenttype", false),
    DATA("data", false)
}
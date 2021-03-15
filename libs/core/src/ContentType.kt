package com.sebbia.cloudevents.core

class ContentType(
    val type: String,
    val subType: String,
    val suffix: String? = null,
    val parameter: String? = null
) {

    object Text {
        val Plain = ContentType("text", "plain")
    }

    object Application {
        val Json = ContentType("application", "json")
        val Protobuf = ContentType("application", "protobuf")
        val OctetStream = ContentType("application", "octet-stream")
    }

    override fun toString(): String =
        StringBuilder().apply {
            append("$type/$subType")
            if (suffix != null)
                append("+$suffix")
            if (parameter != null)
                append(";$parameter")
        }.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContentType

        if (type != other.type) return false
        if (subType != other.subType) return false
        if (suffix != other.suffix) return false
        if (parameter != other.parameter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + subType.hashCode()
        result = 31 * result + (suffix?.hashCode() ?: 0)
        result = 31 * result + (parameter?.hashCode() ?: 0)
        return result
    }
}

private val contentTypeRx = "^([a-z-]+)/([a-z-]+)(\\+([a-z-]+))?(;\\s*(.+))?$".toRegex(RegexOption.IGNORE_CASE)

fun String.toContentType(): ContentType {
    val result = contentTypeRx.matchEntire(this) ?: throw IllegalArgumentException("Invalid content type format")
    return ContentType(
        result.groups[1]!!.value,
        result.groups[2]!!.value,
        result.groups[4]?.value,
        result.groups[6]?.value
    )
}

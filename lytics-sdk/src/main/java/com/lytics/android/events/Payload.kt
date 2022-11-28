package com.lytics.android.events

import com.lytics.android.Constants
import com.lytics.android.JsonSerializable
import com.lytics.android.Lytics
import com.lytics.android.toMap
import org.json.JSONObject

internal data class Payload(
    var id: Long? = null,
    var stream: String,
    var data: Map<String, Any?>? = null,
    var identifiers: Map<String, Any?>? = null,
    var attributes: Map<String, Any?>? = null,
    var properties: Map<String, Any?>? = null,
    var consent: Map<String, Any?>? = null,
) : JsonSerializable {

    constructor(id: Long, stream: String, payload: String) : this(id, stream) {
        val jsonObject = JSONObject(payload)
        val jsonData = jsonObject.toMap()
        identifiers = jsonData[Constants.KEY_IDENTIFIERS] as? Map<String, Any?>
        attributes = jsonData[Constants.KEY_ATTRIBUTES] as? Map<String, Any?>
        properties = jsonData[Constants.KEY_PROPERTIES] as? Map<String, Any?>
        consent = jsonData[Constants.KEY_CONSENT] as? Map<String, Any?>
        data = jsonData.minus(
            listOf(
                Constants.KEY_IDENTIFIERS,
                Constants.KEY_ATTRIBUTES,
                Constants.KEY_PROPERTIES,
                Constants.KEY_CONSENT
            )
        )
    }

    constructor(event: LyticsIdentityEvent) : this(
        stream = event.stream ?: Lytics.configuration.defaultStream,
        identifiers = event.identifiers,
        attributes = event.attributes,
    ) {
        event.name?.let {
            data = mapOf(Constants.KEY_EVENT_NAME to it)
        }
    }

    constructor(event: LyticsConsentEvent) : this(
        stream = event.stream ?: Lytics.configuration.defaultStream,
        identifiers = event.identifiers,
        attributes = event.attributes,
        consent = event.consent,
    ) {
        event.name?.let {
            data = mapOf(Constants.KEY_EVENT_NAME to it)
        }
    }

    constructor(event: LyticsEvent) : this(
        stream = event.stream ?: Lytics.configuration.defaultStream,
        identifiers = event.identifiers,
        properties = event.properties,
    ) {
        event.name?.let {
            data = mapOf(Constants.KEY_EVENT_NAME to it)
        }
    }


    internal fun filterValue(value: Any?): Boolean {
        return when (value) {
            // remove null values
            null -> false
            // remove blank strings
            is String -> value.isNotBlank()
            // otherwise keep
            else -> true
        }
    }

    /**
     * Clean this payload. Removes empty values.
     */
    fun clean() {
        identifiers?.let {
            identifiers = it.filterValues { value -> filterValue(value) }
        }
        attributes?.let {
            attributes  = it.filterValues { value -> filterValue(value) }
        }
        properties?.let {
            properties  = it.filterValues { value -> filterValue(value) }
        }
        consent?.let {
            consent  = it.filterValues { value -> filterValue(value) }
        }
        data?.let {
            data  = it.filterValues { value -> filterValue(value) }
        }
    }

    override fun serialize(): JSONObject {
        val json = data?.let { JSONObject(it) } ?: JSONObject()
        json.apply {
            identifiers?.let {
                put(Constants.KEY_IDENTIFIERS, JSONObject(it))
            }
            attributes?.let {
                put(Constants.KEY_ATTRIBUTES, JSONObject(it))
            }
            properties?.let {
                put(Constants.KEY_PROPERTIES, JSONObject(it))
            }
            consent?.let {
                put(Constants.KEY_CONSENT, JSONObject(it))
            }
        }
        return json
    }
}

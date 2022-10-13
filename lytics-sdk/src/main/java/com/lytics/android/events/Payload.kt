package com.lytics.android.events

import com.lytics.android.Constants
import com.lytics.android.JsonSerializable
import org.json.JSONObject

internal data class Payload(
    var stream: String,
    var eventName: String? = null,
    var identifiers: Map<String, Any?>? = null,
    var attributes: Map<String, Any?>? = null,
    var properties: Map<String, Any?>? = null,
    var consent: Map<String, Any?>? = null,
) : JsonSerializable {
    override fun serialize(): JSONObject {
        return JSONObject().apply {
            eventName?.let {
                put(Constants.KEY_EVENT_NAME, it)
            }
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
    }


}

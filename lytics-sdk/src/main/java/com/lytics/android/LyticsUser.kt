package com.lytics.android

import org.json.JSONObject

data class LyticsUser(
    var identifiers: Map<String, String>? = null,
    var attributes: Map<String, String>? = null,
    var consent: Map<String, String>? = null,
) : JsonSerializable {

    /**
     * Create a Lytics user from a JSON object
     */
    internal constructor(jsonObject: JSONObject) : this() {
        identifiers = jsonObject.optJSONObject(KEY_IDENTIFIERS)?.toStringMap()
        attributes = jsonObject.optJSONObject(KEY_ATTRIBUTES)?.toStringMap()
        consent = jsonObject.optJSONObject(KEY_CONSENT)?.toStringMap()
    }

    /**
     * Convert this Lytics user data class to a JSON object
     */
    override fun serialize(): JSONObject {
        return JSONObject().apply {
            identifiers?.let {
                put(KEY_IDENTIFIERS, JSONObject(it))
            }
            attributes?.let {
                put(KEY_ATTRIBUTES, JSONObject(it))
            }
            consent?.let {
                put(KEY_CONSENT, JSONObject(it))
            }
        }
    }

    companion object {
        const val KEY_IDENTIFIERS = "identifiers"
        const val KEY_ATTRIBUTES = "attributes"
        const val KEY_CONSENT = "consent"
    }
}

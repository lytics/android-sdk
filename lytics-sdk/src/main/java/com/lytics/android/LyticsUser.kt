package com.lytics.android

import org.json.JSONObject

data class LyticsUser(
    var identifiers: Map<String, Any?>? = null,
    var attributes: Map<String, Any?>? = null,
    var consent: Map<String, Any?>? = null,
    var profile: Map<String, Any?>? = null,
) : JsonSerializable {

    /**
     * Create a Lytics user from a JSON object
     */
    internal constructor(jsonObject: JSONObject) : this() {
        identifiers = jsonObject.optJSONObject(Constants.KEY_IDENTIFIERS)?.toMap()
        attributes = jsonObject.optJSONObject(Constants.KEY_ATTRIBUTES)?.toMap()
        consent = jsonObject.optJSONObject(Constants.KEY_CONSENT)?.toMap()
    }

    /**
     * Convert this Lytics user data class to a JSON object
     */
    override fun serialize(): JSONObject {
        return JSONObject().apply {
            identifiers?.let {
                put(Constants.KEY_IDENTIFIERS, JSONObject(it))
            }
            attributes?.let {
                put(Constants.KEY_ATTRIBUTES, JSONObject(it))
            }
            consent?.let {
                put(Constants.KEY_CONSENT, JSONObject(it))
            }
        }
    }
}

package com.lytics.android.events

import org.json.JSONObject

data class LyticsIdentityEvent(
    var stream: String? = null,
    var name: String? = null,
    var identifiers: Map<String, Any?>? = null,
    var attributes: Map<String, Any?>? = null,
    var sendEvent: Boolean = true,
) {
    fun toJson(): JSONObject {
        return JSONObject()
    }
}

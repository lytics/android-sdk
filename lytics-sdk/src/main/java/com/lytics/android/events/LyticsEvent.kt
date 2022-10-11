package com.lytics.android.events

import org.json.JSONObject

data class LyticsEvent(
    var stream: String? = null,
    var name: String? = null,
) {
    fun toJson(): JSONObject {
        return JSONObject()
    }
}
    var identifiers: Map<String, Any?>? = null,
    var properties: Map<String, Any?>? = null

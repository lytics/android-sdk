package com.lytics.android

import org.json.JSONArray
import org.json.JSONObject


/**
 * Extension to JSONObject to add a method to convert a JSON object to a Map<String, String>
 */
fun JSONObject.toMap(): Map<String, Any?> {
    return keys().asSequence().associateWith { key ->
        kotlin.runCatching {
            when (val value = this[key]) {
                is JSONArray -> (0 until value.length()).map { value[it] }
                is JSONObject -> value.toMap()
                JSONObject.NULL -> null
                else -> value
            }
        }.getOrNull()
    }
}

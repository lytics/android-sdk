package com.lytics.android

import org.json.JSONObject

/**
 * Extension to JSONObject to add a method to convert a JSON object to a Map<String, String>
 */
fun JSONObject.toStringMap() : Map<String, String> {
    return keys().asSequence().associateWith { key -> "${this[key]}" }
}

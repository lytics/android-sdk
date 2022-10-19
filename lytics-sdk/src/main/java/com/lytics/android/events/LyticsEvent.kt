package com.lytics.android.events

data class LyticsEvent(
    var stream: String? = null,
    var name: String? = null,
    var identifiers: Map<String, Any?>? = null,
    var properties: Map<String, Any?>? = null
)

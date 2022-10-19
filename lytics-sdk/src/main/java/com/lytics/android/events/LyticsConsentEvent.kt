package com.lytics.android.events

data class LyticsConsentEvent(
    var stream: String? = null,
    var name: String? = null,
    var identifiers: Map<String, Any?>? = null,
    var attributes: Map<String, Any?>? = null,
    var consent: Map<String, Any?>? = null,
    var sendEvent: Boolean = true,
)

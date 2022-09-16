package com.lytics.android

data class LyticsUser(
    var identifiers: Map<String, String>? = null,
    var attributes: Map<String, String>? = null,
)

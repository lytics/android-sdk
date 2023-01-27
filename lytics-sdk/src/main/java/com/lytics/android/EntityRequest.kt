package com.lytics.android

import java.net.URI
import java.net.URL

internal class EntityRequest(private val identifier: EntityIdentifier) : BaseRequest() {
    /**
     * Get the URL for this request
     */
    override fun buildURL(): URL {
        val config = Lytics.configuration
        val url = "${config.entityEndpoint}${config.defaultTable}/${identifier.name}/${identifier.value}"
        return URI.create(url).toURL()
    }
}
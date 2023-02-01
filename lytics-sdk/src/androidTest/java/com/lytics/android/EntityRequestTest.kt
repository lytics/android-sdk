package com.lytics.android

import org.junit.Assert
import org.junit.Test

class EntityRequestTest {
    @Test
    fun testBuildUrl() {
        val config = LyticsConfiguration("API_KEY")
        Lytics.configuration = config

        val identifier = EntityIdentifier("email", "anyone@lytics.com")
        val request = EntityRequest(identifier)

        val url = "${config.entityEndpoint}${config.defaultTable}/email/anyone@lytics.com"
        Assert.assertEquals(url, request.buildURL().toString())
    }
}
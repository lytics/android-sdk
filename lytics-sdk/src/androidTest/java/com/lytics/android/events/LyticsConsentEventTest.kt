package com.lytics.android.events

import org.junit.Assert
import org.junit.Test

class LyticsConsentEventTest {
    @Test
    fun testLyticsConsentEvent() {
        val event = LyticsConsentEvent()

        event.stream = "stream"
        Assert.assertEquals("stream", event.stream)

        event.name = "name"
        Assert.assertEquals("name", event.name)

        val consent = mapOf("consent" to true)
        event.consent = consent
        Assert.assertEquals(consent, event.consent)

        val identifiers = mapOf("identifiers" to 1)
        event.identifiers = identifiers
        Assert.assertEquals(identifiers, event.identifiers)

        val attributes = mapOf("attributes" to "blue")
        event.attributes = attributes
        Assert.assertEquals(attributes, event.attributes)

        event.sendEvent = true
        Assert.assertTrue(event.sendEvent)
    }
}
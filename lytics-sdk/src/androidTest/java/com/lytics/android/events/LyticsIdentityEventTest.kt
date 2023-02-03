package com.lytics.android.events

import org.junit.Assert
import org.junit.Test

class LyticsIdentityEventTest {
    @Test
    fun testLyticsIdentityEvent() {
        val event = LyticsIdentityEvent()

        event.stream = "stream"
        Assert.assertEquals("stream", event.stream)

        event.name = "name"
        Assert.assertEquals("name", event.name)

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
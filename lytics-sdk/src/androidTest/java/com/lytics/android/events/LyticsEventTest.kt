package com.lytics.android.events

import org.junit.Assert
import org.junit.Test

class LyticsEventTest {
    @Test
    fun testLyticsEvent() {
        val event = LyticsEvent()

        event.stream = "stream"
        Assert.assertEquals("stream", event.stream)

        event.name = "name"
        Assert.assertEquals("name", event.name)

        val identifiers = mapOf("identifiers" to 1)
        event.identifiers = identifiers
        Assert.assertEquals(identifiers, event.identifiers)

        val properties = mapOf("properties" to "blue")
        event.properties = properties
        Assert.assertEquals(properties, event.properties)
    }
}
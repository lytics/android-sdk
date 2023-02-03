package com.lytics.android.events

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lytics.android.Constants
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PayloadTest {
    @Test
    fun testSetters() {
        val payload = Payload(stream = "android_sdk")
        payload.stream = "stream"
        payload.id = 1L
        payload.data = mapOf("data" to 1)
        payload.identifiers = mapOf("identifiers" to 2)
        payload.attributes = mapOf("attributes" to 3)
        payload.properties = mapOf("properties" to 4)
        payload.consent = mapOf("consent" to 5)

        Assert.assertEquals("stream", payload.stream)
        Assert.assertEquals(1L, payload.id)
        Assert.assertEquals(mapOf("data" to 1), payload.data)
        Assert.assertEquals(mapOf("identifiers" to 2), payload.identifiers)
        Assert.assertEquals(mapOf("attributes" to 3), payload.attributes)
        Assert.assertEquals(mapOf("properties" to 4), payload.properties)
        Assert.assertEquals(mapOf("consent" to 5), payload.consent)
    }

    @Test
    fun testCleanPayload() {
        val emptyPayload = Payload(stream = "android_sdk")
        emptyPayload.clean()
        Assert.assertNull(emptyPayload.data)
        Assert.assertNull(emptyPayload.identifiers)
        Assert.assertNull(emptyPayload.attributes)
        Assert.assertNull(emptyPayload.properties)
        Assert.assertNull(emptyPayload.consent)

        val uncleanMap = mapOf("empty" to "", "blank" to "  ", "null" to null, "keep" to "true", "one" to 1)
        val payload = Payload(
            stream = "android_sdk",
            identifiers = uncleanMap,
            data = uncleanMap,
            attributes = uncleanMap,
            properties = uncleanMap,
            consent = uncleanMap,
        )
        payload.clean()

        val cleanMap = mapOf("keep" to "true", "one" to 1)
        Assert.assertEquals(cleanMap, payload.identifiers)
        Assert.assertEquals(cleanMap, payload.data)
        Assert.assertEquals(cleanMap, payload.attributes)
        Assert.assertEquals(cleanMap, payload.properties)
        Assert.assertEquals(cleanMap, payload.consent)
    }

    @Test
    fun testSerialize() {
        val emptyPayload = Payload(stream = "android_sdk")
        val emptyJson = emptyPayload.serialize()
        Assert.assertEquals("{}", emptyJson.toString())

        val payload = Payload(
            stream = "android_sdk",
            data = mapOf("data" to 1),
            identifiers = mapOf("identifiers" to 2),
            attributes = mapOf("attributes" to 3),
            properties = mapOf("properties" to 4),
            consent = mapOf("consent" to 5),
        )
        Assert.assertEquals(
            """{"data":1,"identifiers":{"identifiers":2},"attributes":{"attributes":3},"properties":{"properties":4},"consent":{"consent":5}}""",
            payload.serialize().toString()
        )
    }

    @Test
    fun testLyticsEventConstructors() {
        val namedLyticsEvent = LyticsEvent(stream = "android_sdk", name = "lytics_event")
        val namedPayload = Payload(namedLyticsEvent)
        Assert.assertEquals(mapOf(Constants.KEY_EVENT_NAME to "lytics_event"), namedPayload.data)

        val event = LyticsEvent(stream = "android_sdk")
        val payload = Payload(event)
        Assert.assertNull(payload.data)
    }

    @Test
    fun testLyticsIdentityEventConstructors() {
        val namedLyticsEvent = LyticsIdentityEvent(stream = "android_sdk", name = "lytics_identity_event")
        val namedPayload = Payload(namedLyticsEvent)
        Assert.assertEquals(mapOf(Constants.KEY_EVENT_NAME to "lytics_identity_event"), namedPayload.data)

        val event = LyticsIdentityEvent(stream = "android_sdk")
        val payload = Payload(event)
        Assert.assertNull(payload.data)
    }

    @Test
    fun testLyticsConsentEventConstructors() {
        val namedLyticsEvent = LyticsConsentEvent(stream = "android_sdk", name = "lytics_consent_event")
        val namedPayload = Payload(namedLyticsEvent)
        Assert.assertEquals(mapOf(Constants.KEY_EVENT_NAME to "lytics_consent_event"), namedPayload.data)

        val event = LyticsConsentEvent(stream = "android_sdk")
        val payload = Payload(event)
        Assert.assertNull(payload.data)
    }

    @Test
    fun testJsonConstructor() {
        val emptyJsonPayload = Payload(1L, "android_sdk", "")
        Assert.assertEquals(1L, emptyJsonPayload.id)
        Assert.assertEquals("android_sdk", emptyJsonPayload.stream)
        Assert.assertNull(emptyJsonPayload.data)

        val payload = Payload(
            2L,
            "android_sdk",
            """{"data":1,"identifiers":{"identifiers":2},"attributes":{"attributes":3},"properties":{"properties":4},"consent":{"consent":5}}"""
        )
        Assert.assertEquals(2L, payload.id)
        Assert.assertEquals("android_sdk", payload.stream)
        Assert.assertEquals(mapOf("data" to 1), payload.data)
        Assert.assertEquals(mapOf("identifiers" to 2), payload.identifiers)
        Assert.assertEquals(mapOf("attributes" to 3), payload.attributes)
        Assert.assertEquals(mapOf("properties" to 4), payload.properties)
        Assert.assertEquals(mapOf("consent" to 5), payload.consent)
    }

    @Test
    fun testBadJsonConstructor() {
        val payload = Payload(
            2L,
            "android_sdk",
            """{"data":1,"identifiers": 2,"attributes":3,"properties":4,"consent":5}"""
        )
        Assert.assertEquals(2L, payload.id)
        Assert.assertEquals("android_sdk", payload.stream)
        Assert.assertEquals(mapOf("data" to 1), payload.data)
        Assert.assertNull(payload.identifiers)
        Assert.assertNull(payload.attributes)
        Assert.assertNull(payload.properties)
        Assert.assertNull(payload.consent)
    }
}
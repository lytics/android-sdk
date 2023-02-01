package com.lytics.android

import com.lytics.android.events.Payload
import org.junit.Assert
import org.junit.Test

class PayloadRequestTest {
    @Test
    fun testBuildURL() {
        val request = PayloadRequest("https://www.lytics.com/", emptyList())
        Assert.assertEquals("https://www.lytics.com/", request.buildURL().toString())
    }

    @Test
    fun testBuildHeaders() {
        Lytics.configuration = LyticsConfiguration("API_KEY")

        val headers = mapOf(
            "Authorization" to "API_KEY",
            "Connection" to "close",
            "Content-type" to "application/json",
            "Accept" to "application/json",
        )

        val request = PayloadRequest("https://www.lytics.com/", emptyList())
        Assert.assertEquals(headers, request.buildHeaders())
    }

    @Test
    fun testBuildRequestData() {
        val payloads = listOf(
            Payload(stream = LyticsConfiguration.DEFAULT_STREAM, data = mapOf("payload" to 1)),
            Payload(stream = LyticsConfiguration.DEFAULT_STREAM, data = mapOf("payload" to 2)),
        )
        val request = PayloadRequest("https://www.lytics.com/", payloads)

        Assert.assertEquals("""[{"payload":1},{"payload":2}]""", request.buildRequestData())
    }
}
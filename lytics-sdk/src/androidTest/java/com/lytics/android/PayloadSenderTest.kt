package com.lytics.android

import com.lytics.android.events.LyticsEvent
import com.lytics.android.events.Payload
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert
import org.junit.Test


class PayloadSenderTest {

    @Test
    fun testSend() {
        val server = MockWebServer()
        val dispatcher: Dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return if (request.path?.endsWith("ok") == true) {
                    MockResponse().setResponseCode(200)
                } else if (request.path?.endsWith("fail") == true) {
                    MockResponse().setResponseCode(500)
                } else {
                    MockResponse().setResponseCode(404)
                }
            }
        }
        server.dispatcher = dispatcher
        val collectionEndpoint = server.url("/collect/json/").toString()
        Lytics.configuration = LyticsConfiguration("API_KEY", collectionEndpoint = collectionEndpoint)

        val payloads = listOf(
            Payload(LyticsEvent(stream = "ok")),
            Payload(LyticsEvent(stream = "fail")),
            Payload(LyticsEvent(stream = "ok")),
        )
        val payloadSender = PayloadSender(payloads)
        val results = payloadSender.send()
        Assert.assertEquals(2, results.success.size)
        Assert.assertEquals(1, results.failed.size)
    }

    @Test
    fun testBuildStreamUrl() {
        Lytics.configuration = LyticsConfiguration("API_KEY")
        val payloadSender = PayloadSender(emptyList())
        val streamUrl = payloadSender.buildStreamUrl(LyticsConfiguration.DEFAULT_STREAM)

        Assert.assertEquals("https://api.lytics.io/collect/json/android_sdk", streamUrl)

        Lytics.configuration = LyticsConfiguration("API_KEY", sandboxMode = true)
        val sandboxStreamUrl = payloadSender.buildStreamUrl(LyticsConfiguration.DEFAULT_STREAM)

        Assert.assertEquals("https://api.lytics.io/collect/json/android_sdk?dryrun=true", sandboxStreamUrl)
    }
}
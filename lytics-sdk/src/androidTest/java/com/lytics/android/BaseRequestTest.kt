package com.lytics.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lytics.android.logging.AndroidLogger
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.TimeUnit


private class TestRequest(
    private val url: String = "https://www.lytics.com/",
    method: RequestMethod = RequestMethod.GET
) : BaseRequest(method) {
    override fun buildURL(): URL {
        return URL(url)
    }

    override fun buildRequestData(): String? {
        return when(requestMethod) {
            RequestMethod.GET -> super.buildRequestData()
            RequestMethod.POST -> "POST THIS"
        }
    }
}

@RunWith(AndroidJUnit4::class)
class BaseRequestTest {
    @Before
    fun setUp() {
        Lytics._configuration = LyticsConfiguration("API_KEY")
        Lytics.logger = AndroidLogger
    }

    @Test
    fun testRequestMethod() {
        Assert.assertFalse(RequestMethod.GET.output)
        Assert.assertEquals("GET", RequestMethod.GET.method)

        Assert.assertTrue(RequestMethod.POST.output)
        Assert.assertEquals("POST", RequestMethod.POST.method)
    }

    @Test
    fun testBuildURL() {
        val request = TestRequest()
        Assert.assertEquals("https://www.lytics.com/", request.buildURL().toString())
    }

    @Test
    fun testBadUrl() {
        val request = TestRequest("")
        val response = request.send()
        Assert.assertEquals(0, response.statusCode)
        Assert.assertEquals("java.net.MalformedURLException: no protocol: ", response.data)
    }

    @Test
    fun testBuildHeaders() {
        val request = TestRequest()
        Assert.assertEquals(
            mapOf(
                "Authorization" to "API_KEY",
                "Connection" to "close",
            ), request.buildHeaders()
        )
    }

    @Test
    fun testBuildRequestData() {
        val request = TestRequest()
        Assert.assertNull(request.buildRequestData())
        Assert.assertEquals(RequestMethod.GET, request.requestMethod)
    }

    @Test
    fun testSendSuccess() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("hello, world!"))
        val baseUrl = server.url("/collect/json/")
        val request = TestRequest(baseUrl.toString())

        val response = request.send()

        Assert.assertEquals(200, response.statusCode)
        Assert.assertEquals("hello, world!", response.data)

        val recordedRequest = server.takeRequest()
        Assert.assertEquals("GET", recordedRequest.method)
        Assert.assertEquals("API_KEY", recordedRequest.headers["Authorization"])
    }

    @Test
    fun testSendFail() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(500).setBody("server error!"))
        val baseUrl = server.url("/collect/json/")
        val request = TestRequest(baseUrl.toString())

        val response = request.send()

        Assert.assertEquals(500, response.statusCode)
        Assert.assertEquals("server error!", response.data)

        val recordedRequest = server.takeRequest()
        Assert.assertEquals("GET", recordedRequest.method)
        Assert.assertEquals("API_KEY", recordedRequest.headers["Authorization"])
    }

    @Test
    fun testTimeout() {
        Lytics._configuration = LyticsConfiguration(
            "API_KEY",
            networkRequestTimeout = TimeUnit.SECONDS.toMillis(1).toInt(),
        )
        val server = MockWebServer()
        server.enqueue(MockResponse().setHeadersDelay(2, TimeUnit.SECONDS))
        val baseUrl = server.url("/collect/json/")
        val request = TestRequest(baseUrl.toString())

        val response = request.send()

        Assert.assertEquals(0, response.statusCode)
        Assert.assertEquals("java.net.SocketTimeoutException: timeout", response.data)
    }

    @Test
    fun testSendWithRetrySuccess() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(200))
        val baseUrl = server.url("/collect/json/")
        val request = TestRequest(baseUrl.toString())

        val response = request.sendWithRetry(1)

        Assert.assertTrue(response.isOk)
        Assert.assertEquals(2, server.requestCount)
    }

    @Test
    fun testSendWithRetryFailure() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(400))
        server.enqueue(MockResponse().setResponseCode(404))
        val baseUrl = server.url("/collect/json/")
        val request = TestRequest(baseUrl.toString())

        val response = request.sendWithRetry(2)

        Assert.assertFalse(response.isOk)
        Assert.assertEquals(3, server.requestCount)
    }

    @Test
    fun testSendWithRetryFailureNoRetries() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(400))
        server.enqueue(MockResponse().setResponseCode(404))
        val baseUrl = server.url("/collect/json/")
        val request = TestRequest(baseUrl.toString())

        val response = request.sendWithRetry()

        Assert.assertFalse(response.isOk)
        Assert.assertEquals(1, server.requestCount)
    }

    @Test
    fun testSendPostData() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(200))
        val baseUrl = server.url("/collect/json/")

        val request = TestRequest(baseUrl.toString(), RequestMethod.POST)
        val response = request.send()

        Assert.assertTrue(response.isOk)
        Assert.assertEquals(1, server.requestCount)

        val recordedRequest = server.takeRequest()
        val requestBody = BufferedReader(InputStreamReader(recordedRequest.body.inputStream())).use { it.readText() }
        Assert.assertEquals("POST", recordedRequest.method)
        Assert.assertEquals("POST THIS", requestBody)
    }
}
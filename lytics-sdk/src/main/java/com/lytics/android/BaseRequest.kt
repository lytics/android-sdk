package com.lytics.android

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

internal enum class RequestMethod(val method: String, val output: Boolean = false) {
    GET("GET", output = false),
    POST("POST", true),
}

internal abstract class BaseRequest(val requestMethod: RequestMethod = RequestMethod.GET) {

    /**
     * Build the URL for this request
     *
     * @return the URL for this request
     */
    abstract fun buildURL(): URL

    /**
     * Build headers for this request
     */
    open fun buildHeaders(): Map<String, String> {
        return mapOf(
            "Authorization" to Lytics.configuration.apiKey,
            "Connection" to "close",
        )
    }

    /**
     * Open the HTTP connection
     */
    open fun open(): HttpURLConnection {
        return buildURL().openConnection() as HttpURLConnection
    }

    /**
     * Create the HTTP request connection
     */
    open fun createConnection(): HttpURLConnection {
        val connection = open()

        val headers = buildHeaders()
        for ((headerName, headerValue) in headers.entries) {
            connection.setRequestProperty(headerName, headerValue)
        }

        connection.requestMethod = requestMethod.method
        connection.doOutput = requestMethod.output
        connection.connectTimeout = Lytics.configuration.networkRequestTimeout
        connection.readTimeout = Lytics.configuration.networkRequestTimeout

        connection.connect()
        return connection
    }

    /**
     * For request methods with data to send, build the data to send.
     * If no output data, return null
     */
    open fun buildRequestData(): String? {
        return null
    }

    /**
     * Read response and log it
     *
     * @param connection the HttpURLConnection
     */
    private fun readAndLog(connection: HttpURLConnection): Response {
        try {
            val responseCode = connection.responseCode
            return if (responseCode in 200..299) {
                Lytics.logger?.info("Request successfully sent: $responseCode")
                val response = getResponseFromConnection(connection)
                Lytics.logger?.debug(response)
                Response(responseCode, response)
            } else {
                Lytics.logger?.error("Request failed: $responseCode")
                val error = getErrorFromConnection(connection)
                Lytics.logger?.debug(error)
                Response(responseCode, error)
            }
        } catch (e: IOException) {
            Lytics.logger?.error(e, "Error reading and logging the response stream")
        }
        return Response()
    }

    /**
     * Reads the API response from the connection stream
     *
     * @param connection the HttpURLConnection
     * @return the request response
     */
    private fun getResponseFromConnection(connection: HttpURLConnection): String {
        return try {
            BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
        } catch (e: IOException) {
            "Error reading connection input stream"
        }
    }

    /**
     * Reads the error message from the connection error stream
     *
     * @param connection the HttpURLConnection
     * @return the error message
     */
    private fun getErrorFromConnection(connection: HttpURLConnection): String {
        return try {
            BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
        } catch (e: IOException) {
            "Error reading connection error stream"
        }
    }

    /**
     * Closes the Response stream and disconnect the connection
     *
     * @param connection the HttpURLConnection
     */
    private fun closeAndDisconnect(connection: HttpURLConnection?) {
        try {
            connection?.inputStream?.close()
        } catch (ignored: IOException) {
            // connection is already closed
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * connect and send the event payload data
     */
    fun send(): Response {
        var connection: HttpURLConnection? = null
        try {
            connection = createConnection()
            buildRequestData()?.let { requestData ->
                DataOutputStream(connection.outputStream).use { it.write(requestData.encodeToByteArray()) }
            }
            return readAndLog(connection)
        } catch (e: IOException) {
            Lytics.logger?.error(e, "Error sending payload to ${connection?.url}")
            if (e is UnknownHostException) {
                Lytics.logger?.error("$e")
            }
        } finally {
            closeAndDisconnect(connection)
        }
        return Response()
    }

    fun sendWithRetry(maxRetries: Int = 0): Response {
        var response: Response? = null
        for (i in 0..maxRetries) {
            response = send()
            if (response.isOk) {
                return response
            } else {
                Lytics.logger?.warn("Retrying ${i + 1} of $maxRetries")
            }
        }
        return response ?: Response()
    }
}
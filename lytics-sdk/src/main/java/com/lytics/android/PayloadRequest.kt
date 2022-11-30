package com.lytics.android

import com.lytics.android.events.Payload
import org.json.JSONArray
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.UnknownHostException

/**
 * A HTTP request for a payload
 */
internal class PayloadRequest(
    private val url: String,
    private val payloads: List<Payload>
) {
    /**
     * Get the URL for this request
     */
    private fun buildURL(): URL {
        return URI.create(url).toURL()
    }

    /**
     * Build headers for this request
     */
    private fun buildHeaders(): Map<String, String> {
        return mapOf(
            "Content-type" to "application/json",
            "Accept" to "application/json",
            "Authorization" to Lytics.configuration.apiKey,
            "Connection" to "close",
        )
    }

    /**
     * Open the HTTP connection
     */
    private fun open(): HttpURLConnection {
        return buildURL().openConnection() as HttpURLConnection
    }

    /**
     * Create the HTTP request connection
     */
    private fun createConnection(): HttpURLConnection {
        val connection = open()

        val headers = buildHeaders()
        for ((headerName, headerValue) in headers.entries) {
            connection.setRequestProperty(headerName, headerValue)
        }

        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = Lytics.configuration.networkRequestTimeout
        connection.readTimeout = Lytics.configuration.networkRequestTimeout

        connection.connect()
        return connection
    }

    /**
     * connect and send the event payload data
     */
    fun send(): Boolean {
        var connection: HttpURLConnection? = null
        try {
            connection = createConnection()
            val requestData = buildRequestData()
            DataOutputStream(connection.outputStream).use { it.write(requestData.encodeToByteArray()) }
            return readAndLog(connection)
        } catch (e: IOException) {
            Lytics.logger?.error(e, "Error sending payload to $url")
            if (e is UnknownHostException) {
                Lytics.logger?.error("$e")
            }
        } finally {
            closeAndDisconnect(connection)
        }
        return false
    }

    /**
     * Read response and log it
     *
     * @param connection the HttpURLConnection
     */
    private fun readAndLog(connection: HttpURLConnection): Boolean {
        try {
            val responseCode = connection.responseCode
            return if (responseCode in 200..299) {
                Lytics.logger?.info("Payload successfully sent: $responseCode")
                val response = getResponseFromConnection(connection)
                Lytics.logger?.debug(response)
                true
            } else {
                Lytics.logger?.error("Request failed: $responseCode")
                val error = getErrorFromConnection(connection)
                Lytics.logger?.debug(error)
                false
            }
        } catch (e: IOException) {
            Lytics.logger?.error(e, "Error reading and logging the response stream")
        }
        return false
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
     * Builds a JSON array of all the payloads
     *
     * @return JSON array of all payloads as a String
     */
    private fun buildRequestData(): String {
        val jsonPayloadObjects = payloads.map { it.serialize() }
        val jsonPayloads = JSONArray(jsonPayloadObjects)
        return jsonPayloads.toString()
    }
}
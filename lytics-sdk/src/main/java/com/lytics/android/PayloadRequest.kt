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
) : BaseRequest(requestMethod = RequestMethod.POST) {
    /**
     * Get the URL for this request
     */
    override fun buildURL(): URL {
        return URI.create(url).toURL()
    }

    /**
     * Build headers for this request
     */
    override fun buildHeaders(): Map<String, String> {
        return super.buildHeaders().plus(
            mapOf(
                "Content-type" to "application/json",
                "Accept" to "application/json",
            )
        )
    }

    /**
     * Builds a JSON array of all the payloads
     *
     * @return JSON array of all payloads as a String
     */
    override fun buildRequestData(): String {
        val jsonPayloadObjects = payloads.map { it.serialize() }
        val jsonPayloads = JSONArray(jsonPayloadObjects)
        return jsonPayloads.toString()
    }
}
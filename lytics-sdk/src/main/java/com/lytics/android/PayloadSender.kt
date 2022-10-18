package com.lytics.android

import com.lytics.android.events.Payload

/**
 * Sends payloads to collection endpoints
 */
internal class PayloadSender(private val payloads: Collection<Payload>) {

    data class Results(val success: List<Payload>, val failed: List<Payload>)

    /**
     * Groups the payload by stream and sends each group to the configured collection endpoints.
     *
     * @return results of which payloads successfully sent and those that did not
     */
    fun send(): Results {
        val successPayloads = mutableListOf<Payload>()
        val failedPayloads = mutableListOf<Payload>()
        val groupedPayloads = payloads.groupBy { it.stream }
        groupedPayloads.keys.forEach { stream ->
            val streamPayloads = groupedPayloads[stream]
            streamPayloads?.let {
                Lytics.logger.debug("Sending ${it.size} payloads to $stream")
                val success = sendStreamPayloads(stream, it)
                if (success) {
                    successPayloads.addAll(it)
                } else {
                    failedPayloads.addAll(it)
                }
            }
        }
        return Results(successPayloads, failedPayloads)
    }

    /**
     * Send the given list of payloads to the given stream.
     *
     * @return returns true if successfully uploaded events to the stream, false if failed.
     * If all failed, return false
     */
    private fun sendStreamPayloads(stream: String, streamPayloads: List<Payload>): Boolean {
        val queryString =  if (Lytics.configuration.sandboxMode) "?dryrun" else ""
        val streamUrl = "${Lytics.configuration.collectionEndpoint}$stream$queryString"

        val payloadRequest = PayloadRequest(streamUrl, streamPayloads)
        return payloadRequest.send()
    }
}
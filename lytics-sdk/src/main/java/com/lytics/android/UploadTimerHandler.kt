package com.lytics.android

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.lytics.android.logging.Logger

/**
 * A handler which processes delayed messages based on the configured upload interval to trigger a dispatch
 */
internal class UploadTimerHandler(looper: Looper) : Handler(looper) {

    /**
     * Trigger the Lytics SDK dispatch when a message is processed
     */
    override fun handleMessage(msg: Message) {
        Lytics.logger?.debug("UploadTimerHandler message: ${msg.what}")
        if (msg.what == DISPATCH_QUEUE) {
            Lytics.logger?.debug("UploadTimerHandler dispatch()")
            Lytics.dispatch()
        }
    }

    companion object {
        /**
         * Message ID for telling Lytics SDK to dispatch the queue
         */
        const val DISPATCH_QUEUE = 1
    }
}
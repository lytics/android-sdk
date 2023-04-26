package com.lytics.android

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lytics.android.logging.AndroidLogger
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class UploadTimerHandlerTest {
    @Test
    fun testValidMessageDispatch() {
        val mockLytics = mock<ILytics> {
            on { configuration } doReturn LyticsConfiguration("API_KEY")
            on { logger } doReturn AndroidLogger
        }

        val uploadTimerHandler = UploadTimerHandler(mockLytics, Looper.getMainLooper())

        val message = uploadTimerHandler.obtainMessage(UploadTimerHandler.DISPATCH_QUEUE)
        uploadTimerHandler.handleMessage(message)

        verify(mockLytics, times(1)).dispatch()
    }

    @Test
    fun testInvalidMessage() {
        val mockLytics = mock<ILytics> {
            on { configuration } doReturn LyticsConfiguration("API_KEY")
        }

        val uploadTimerHandler = UploadTimerHandler(mockLytics, Looper.getMainLooper())

        val invalidMessage = uploadTimerHandler.obtainMessage(2)
        uploadTimerHandler.handleMessage(invalidMessage)

        verify(mockLytics, times(0)).dispatch()
    }
}
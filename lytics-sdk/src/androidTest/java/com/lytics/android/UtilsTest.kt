package com.lytics.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lytics.android.Utils.streamify
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UtilsTest {
    @Test
    fun testStreamify() {
        val samples = mapOf(" Test Stream " to "Test_Stream", "TEST           STREAM" to "TEST_STREAM")
        samples.forEach { originalStream, cleanedStream ->
            Assert.assertEquals(cleanedStream, streamify(originalStream))
        }
    }
}
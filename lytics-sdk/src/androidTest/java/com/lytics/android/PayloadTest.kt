package com.lytics.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lytics.android.events.Payload
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PayloadTest {
    @Test
    fun testCleanPayload() {
        val payload = Payload(
            stream = "android_sdk",
            identifiers = mapOf("empty" to "", "blank" to "  ", "null" to null, "keep" to "true", "one" to 1)
        )
        payload.clean()
        Assert.assertEquals(mapOf("keep" to "true", "one" to 1), payload.identifiers)
    }
}
package com.lytics.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResponseTest {
    @Test
    fun testIsOk() {
        Assert.assertFalse(Response().isOk) // status code is 0
        Assert.assertTrue(Response(statusCode = 1).isOk)
        Assert.assertEquals(Response(statusCode = 1).statusCode, 1)
        Assert.assertTrue(Response(statusCode = 200).isOk)
        Assert.assertEquals(Response(statusCode = 200).statusCode, 200)
        Assert.assertTrue(Response(statusCode = 399).isOk)
        Assert.assertFalse(Response(statusCode = 400).isOk)
        Assert.assertFalse(Response(statusCode = 404).isOk)
        Assert.assertFalse(Response(statusCode = 500).isOk)
    }

    @Test
    fun testToJson() {
        Assert.assertNull(Response().json)  // data is null
        Assert.assertNull(Response(data = "not json").json)  // just a string
        Assert.assertEquals(Response(data = "not json").data, "not json")
        val json = Response(data = "{\"test\": 1}").json
        Assert.assertNotNull(json)
        Assert.assertEquals(mapOf("test" to 1), json?.toMap())
    }
}
package com.lytics.android

import org.junit.Assert
import org.junit.Test

class BaseRequestTest {
    @Test
    fun testRequestMethod() {
        Assert.assertFalse(RequestMethod.GET.output)
        Assert.assertEquals("GET", RequestMethod.GET.method)

        Assert.assertTrue(RequestMethod.POST.output)
        Assert.assertEquals("POST", RequestMethod.POST.method)
    }
}
package com.lytics.android

import org.junit.Assert
import org.junit.Test

class EntityIdentifierTest {
    @Test
    fun testEntityIdentifier() {
        val identifier = EntityIdentifier("name", "value")
        Assert.assertEquals("name", identifier.name)
        Assert.assertEquals("value", identifier.value)
    }
}
package com.lytics.android

import java.util.*

internal object Utils {
    /**
     * Generates a random UUID string
     *
     * @return a random UUID string. ex "25eebcba-5ec9-43fe-9179-dafd8d8dd157"
     */
    fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }
}

package com.lytics.android

/**
 * A field name and value used to identify an entity.
 */
data class EntityIdentifier(
    /**
     * The name of the identity field, typically the primary identity key which defaults to `_uid`
     */
    val name: String,
    /**
     * The value of the identity field, a user specific value, such as an email `bob@gmail.com`
     */
    val value: String,
)

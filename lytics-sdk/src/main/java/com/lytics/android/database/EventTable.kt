package com.lytics.android.database

import android.provider.BaseColumns

/**
 * Constants for the event queue table SQL
 */
internal object EventTable {
    object Columns : BaseColumns {
        const val TABLE_NAME = "events"

        /**
         * The status of this event payload
         */
        const val COLUMN_NAME_STATUS = "status"

        /**
         * A counter for how many attempts to upload a payload has failed
         */
        const val COLUMN_NAME_RETRY_COUNT = "retry_count"

        /**
         * The stream to upload this payload to
         */
        const val COLUMN_NAME_STREAM = "stream"

        /**
         * The JSON of the event payload to upload
         */
        const val COLUMN_NAME_PAYLOAD = "payload"
    }

    object EventStatus {
        /**
         * event payload is awaiting upload
         */
        const val PENDING = 0

        /**
         * event payload is currently being uploaded
         */
        const val PROCESSING = 1
    }

    /**
     * create events table SQL
     */
    const val SQL_CREATE_EVENTS = "CREATE TABLE ${Columns.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "${Columns.COLUMN_NAME_STATUS} INTEGER DEFAULT ${EventStatus.PENDING}, " +
            "${Columns.COLUMN_NAME_RETRY_COUNT} INTEGER DEFAULT 0, " +
            "${Columns.COLUMN_NAME_STREAM} TEXT, " +
            "${Columns.COLUMN_NAME_PAYLOAD} TEXT" +
            ")"

    /**
     * SQL to drop the events table
     */
    const val SQL_DROP_EVENTS = "DROP TABLE IF EXISTS ${Columns.TABLE_NAME}"
}
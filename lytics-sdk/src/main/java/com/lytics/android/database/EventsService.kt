package com.lytics.android.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import com.lytics.android.Lytics
import com.lytics.android.events.Payload

/**
 * A service to manage events in the database queue
 */
internal object EventsService {

    /**
     * Insert a payload into the database
     */
    fun insertPayload(db: SQLiteDatabase, payload: Payload) {
        val values = ContentValues().apply {
            put(EventTable.Columns.COLUMN_NAME_STREAM, payload.stream)
            put(EventTable.Columns.COLUMN_NAME_PAYLOAD, payload.serialize().toString())
        }
        db.insert(EventTable.Columns.TABLE_NAME, null, values)
    }

    /**
     * Return the number of payloads that are currently pending upload
     */
    fun getPendingPayloadCount(db: SQLiteDatabase): Int {
        var cursor: Cursor? = null

        return try {
            cursor = db.query(
                EventTable.Columns.TABLE_NAME,
                null,
                "${EventTable.Columns.COLUMN_NAME_STATUS} = ? ",
                arrayOf(EventTable.EventStatus.PENDING.toString()),
                null,
                null,
                null
            )
            cursor.count
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }
    }

    /**
     * Get pending payloads to upload
     */
    fun getPendingPayloads(db: SQLiteDatabase): List<Payload> {
        val projection = arrayOf(
            BaseColumns._ID,
            EventTable.Columns.COLUMN_NAME_STREAM,
            EventTable.Columns.COLUMN_NAME_PAYLOAD
        )
        val selection = "${EventTable.Columns.COLUMN_NAME_STATUS} = ? "
        val selectionArgs = arrayOf(EventTable.EventStatus.PENDING.toString())

        var cursor: Cursor? = null
        return try {
            db.beginTransaction()
            cursor = db.query(
                EventTable.Columns.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
            )
            val pendingPayloads = mutableListOf<Payload>()
            with(cursor) {
                val idColumn = getColumnIndexOrThrow(BaseColumns._ID)
                val streamColumn = getColumnIndexOrThrow(EventTable.Columns.COLUMN_NAME_STREAM)
                val payloadColumn = getColumnIndexOrThrow(EventTable.Columns.COLUMN_NAME_PAYLOAD)
                while (moveToNext()) {
                    val id = getLong(idColumn)
                    val stream = getString(streamColumn)
                    val payloadJson = getString(payloadColumn)
                    val payload = Payload(id, stream, payload = payloadJson)
                    pendingPayloads.add(payload)
                }
            }
            val pendingPayloadIds = pendingPayloads.mapNotNull { it.id }
            updateEventStatus(db, EventTable.EventStatus.PROCESSING, pendingPayloadIds)
            db.setTransactionSuccessful()
            pendingPayloads
        } finally {
            db.endTransaction()
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }
    }

    /**
     * Update the given payloads status as processing/uploading
     */
    fun updateEventStatus(db: SQLiteDatabase, status: Int, payloadPrimaryKeys: Collection<Long>) {
        val sql = "UPDATE ${EventTable.Columns.TABLE_NAME} " +
                "SET ${EventTable.Columns.COLUMN_NAME_STATUS} = $status " +
                "WHERE ${BaseColumns._ID} IN (${payloadPrimaryKeys.joinToString()})"
        db.execSQL(sql)
    }

    /**
     * For payloads that failed to upload, return them to pending status and increment retry count
     */
    fun failedPayloads(db: SQLiteDatabase, payloadPrimaryKeys: Collection<Long>) {
        Lytics.logger?.debug("updated ${payloadPrimaryKeys.size} failed events for retry")
        val sql = "UPDATE ${EventTable.Columns.TABLE_NAME} " +
                "SET ${EventTable.Columns.COLUMN_NAME_STATUS} = ${EventTable.EventStatus.PENDING}, " +
                "${EventTable.Columns.COLUMN_NAME_RETRY_COUNT} = " +
                "(${EventTable.Columns.COLUMN_NAME_RETRY_COUNT} + 1) " +
                "WHERE ${BaseColumns._ID} IN (${payloadPrimaryKeys.joinToString()})"
        db.execSQL(sql)

        clearMaxRetries(db)
    }

    /**
     * Clears payloads that have exceeded max upload retries
     */
    fun clearMaxRetries(db: SQLiteDatabase) {
        val selection = "${EventTable.Columns.COLUMN_NAME_RETRY_COUNT} > ?"
        val selectionArgs = arrayOf("${Lytics.configuration.maxRetryCount}")
        val count = db.delete(EventTable.Columns.TABLE_NAME, selection, selectionArgs)
        Lytics.logger?.debug("removed $count events for exceeding max retry count ${Lytics.configuration.maxRetryCount}")
    }

    /**
     * For payloads that successfully uploaded, remove them from the database queue
     */
    fun processedPayloads(db: SQLiteDatabase, payloadPrimaryKeys: Collection<Long>) {
        val placeholders = CharArray(payloadPrimaryKeys.size) { '?' }
        val selection = "${BaseColumns._ID} IN (${placeholders.joinToString()})"
        val selectionArgs = payloadPrimaryKeys.map { it.toString() }
        val count = db.delete(EventTable.Columns.TABLE_NAME, selection, selectionArgs.toTypedArray())
        Lytics.logger?.debug("processed event count $count")
    }

    /**
     * Clear all events from the database queue
     */
    fun clearAll(db: SQLiteDatabase) {
        val count = db.delete(EventTable.Columns.TABLE_NAME, "1", arrayOf())
        Lytics.logger?.debug("Cleared $count events from the queue")
    }
}
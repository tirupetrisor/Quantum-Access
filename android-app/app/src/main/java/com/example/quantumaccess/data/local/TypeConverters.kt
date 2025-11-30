package com.example.quantumaccess.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.util.UUID

/**
 * Converters pentru tipuri complexe Room.
 * Handles UUID <-> String and Instant <-> Long (epoch millis).
 */
class TypeConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }

    @TypeConverter
    fun fromUUID(uuid: String?): UUID? {
        return uuid?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun uuidToString(uuid: UUID?): String? {
        return uuid?.toString()
    }
}


package com.thewalkersoft.tracker.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDate

class DateConverters {
    @TypeConverter
    fun fromString(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun dateToString(date: LocalDate?): String? {
        return date?.toString()
    }
}

package com.coachapp.data.local

import androidx.room.TypeConverter

class CoachTypeConverters {
    @TypeConverter
    fun fromMuscles(muscles: List<String>): String {
        return muscles.joinToString(separator = "\n")
    }

    @TypeConverter
    fun toMuscles(value: String): List<String> {
        return value
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}

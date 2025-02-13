package com.example.project2.data.local_db

import androidx.room.TypeConverter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromList(value: List<String>?): String {
        return Gson().toJson(value) // ממיר רשימה למחרוזת JSON
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        val listType = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}
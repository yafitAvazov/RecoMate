package com.example.project2.data.model

import android.content.Context
import com.example.project2.R

object CategoryMapper {
    private val categoryMap = mapOf(
        1 to R.string.fashion,  // אופנה
        2 to R.string.food,     // אוכל
        3 to R.string.game,     // משחקים
        4 to R.string.home,     // בית
        5 to R.string.tech,     // טכנולוגיה
        6 to R.string.sport,    // ספורט
        7 to R.string.travel,   // טיולים
        8 to R.string.beauty,   // יופי
        9 to R.string.book,     // ספרים
        10 to R.string.shops,   // חנויות
        11 to R.string.movie,   // סרטים
        12 to R.string.health   // בריאות
    )

    fun getCategoryId(localizedCategory: String, context: Context): Int? {
        return categoryMap.entries.find { (_, resId) ->
            context.getString(resId) == localizedCategory
        }?.key
    }

    fun getLocalizedCategory(categoryId: Int, context: Context): String {
        return categoryMap[categoryId]?.let { resId -> context.getString(resId) } ?: "Unknown"
    }
}

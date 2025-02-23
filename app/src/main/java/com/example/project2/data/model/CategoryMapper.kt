package com.example.project2.data.model

import android.content.Context
import com.example.project2.R

object CategoryMapper {
    private val categoryMap = mapOf(
        1 to R.string.fashion,
        2 to R.string.food,
        3 to R.string.game,
        4 to R.string.home,
        5 to R.string.tech,
        6 to R.string.sport,
        7 to R.string.travel,
        8 to R.string.beauty,
        9 to R.string.book,
        10 to R.string.shops,
        11 to R.string.movie,
        12 to R.string.health
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

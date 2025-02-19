package com.example.project2.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "review_table")
data class Item(
    @PrimaryKey val id: String = "",
    @ColumnInfo(name = "item_title") val title: String = "",
    @ColumnInfo(name = "item_comment") val comment: String = "",
    @ColumnInfo(name = "item_photo") val photo: String? = null,
    @ColumnInfo(name = "item_price") val price: Double = 0.0,
    @ColumnInfo(name = "item_category") val category: String = "",
    @ColumnInfo(name = "item_link") val link: String = "",
    @ColumnInfo(name = "item_rating") val rating: Int = 0,
    @ColumnInfo(name = "item_address") val address: String? = null,
    @ColumnInfo(name = "item_comments") val comments: List<String> = emptyList(),
    @ColumnInfo(name = "user_id") val userId: String = "",
    @ColumnInfo(name = "isLiked") var isLiked: Boolean = false // 🔥 פשוט, ברור, בלי `Map` // ✅ תיקון נכון!
) : Parcelable



package com.example.project2.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "review_table")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Auto-generated primary key

    @ColumnInfo(name = "item_title") val title: String, // Custom column name for title
    @ColumnInfo(name = "item_comment") val comment: String, // Custom column name for comment
    @ColumnInfo(name = "item_photo") val photo: String?, // Nullable field for photo
    @ColumnInfo(name = "item_price") val price: Double, // Custom column name for price
    @ColumnInfo(name = "item_category") val category: String, // Custom column name for category
    @ColumnInfo(name = "item_link") val link: String, // Custom column name for link
    @ColumnInfo(name = "item_rating") val rating: Int, // Custom column name for rating
    @ColumnInfo(name = "item_address") val address: String? // Nullable field for address
) : Parcelable
/*
object ItemManager{
    val items:MutableList<Item> = mutableListOf()
    fun  add(item: Item){
        items.add(item)
    }

    fun remove(index:Int){
        items.removeAt(index)
    }
}
 */
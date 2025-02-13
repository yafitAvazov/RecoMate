package com.example.project2.data.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.project2.data.model.Item


@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addItem(item: Item)

    @Delete
    fun deleteItem(vararg item: Item)

    @Update

    fun updateItem(item: Item)


    @Query("SELECT * FROM review_table ORDER BY id ASC")
    fun getItems() : LiveData<List<Item>>

    @Query("SELECT * FROM review_table WHERE id LIKE :id")
    fun getItem(id:Int) : LiveData<Item>



    @Query("UPDATE review_table SET item_comments = :comments WHERE id = :id")
    fun updateComments(id: Int, comments: String)


    @Query( "DELETE FROM review_table")
    fun deleteAll()
    @Query("""
    SELECT * FROM review_table 
    WHERE (:selectedCategories IS NULL OR item_category LIKE '%' || :selectedCategories || '%')
    AND (:selectedRating IS NULL OR (item_rating > 0 AND item_rating <= :selectedRating))
    AND (:selectedMinPrice = 0.0 OR item_price <= :selectedMinPrice)
""")


    suspend fun getFilteredItems(
        selectedCategories: String?,
        selectedRating: Int,
        selectedMinPrice: Double
    ): List<Item>






}
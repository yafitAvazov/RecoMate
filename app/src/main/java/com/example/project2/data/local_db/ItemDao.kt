package com.example.project2.data.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.project2.data.model.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Query("SELECT * FROM review_table WHERE id = :itemId")
    fun getItemById(itemId: String): Flow<Item?>

    @Query("SELECT * FROM review_table ORDER BY id DESC")
    fun getItems(): Flow<List<Item>>

    @Query("SELECT * FROM review_table WHERE id LIKE :id")
    fun getItem(id: Int): LiveData<Item>

    @Query("SELECT * FROM review_table WHERE ',' || likedBy || ',' LIKE '%,' || :userId || ',%'")
    fun getUserFavorites(userId: String): Flow<List<Item>>


    @Query("UPDATE review_table SET likedBy = :likedBy WHERE id = :itemId")
    suspend fun updateLikeStatus(itemId: String, likedBy: String)


    // ✅ Update comments using JSON string
    @Query("UPDATE review_table SET item_comments = :commentsJson WHERE id = :itemId")
    suspend fun updateComments(itemId: String, commentsJson: String)

    // ✅ Fetch items by category filter
    @Query("SELECT * FROM review_table WHERE :selectedCategories IS NULL OR ',' || REPLACE(item_category, ', ', ',') || ',' LIKE '%,' || :selectedCategories || ',%'")
    fun getItemsByCategory(selectedCategories: String?): Flow<List<Item>>

    // ✅ Delete all items from the database
    @Query("DELETE FROM review_table")
    suspend fun deleteAll()

    // ✅ Filter items by rating & max price
    @Query("SELECT * FROM review_table WHERE item_rating >= :selectedRating AND item_price <= :selectedMaxPrice")
    fun getFilteredItems(selectedRating: Int, selectedMaxPrice: Double): Flow<List<Item>>
}








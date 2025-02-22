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
    fun getItems(): Flow<List<Item>> // ✅ צריך להחזיר Flow<List<Item>>


    @Query("SELECT * FROM review_table WHERE id LIKE :id")
    fun getItem(id:Int) : LiveData<Item>

    @Query("SELECT * FROM review_table WHERE isLiked = 1")
    fun getUserFavorites(): Flow<List<Item>>

    @Query("UPDATE review_table SET isLiked = :isLiked WHERE id = :itemId")
    suspend fun updateLikeStatus(itemId: Int, isLiked: Boolean)

    @Query("SELECT * FROM review_table WHERE isLiked = 1 AND user_id = :userId")
    fun getUserFavorites(userId: String): Flow<List<Item>>

    @Query("UPDATE review_table SET isLiked = :isLiked WHERE id = :itemId")
    suspend fun updateLikeStatus(itemId: String, isLiked: Boolean)


//    @Query("UPDATE review_table SET isLiked = 1 WHERE id = :itemId AND user_id = :userId")
//    suspend fun addFavorite(itemId: Int, userId: String)
//
//    @Query("UPDATE review_table SET isLiked = 0 WHERE id = :itemId AND user_id = :userId")
//    suspend fun removeFavorite(itemId: Int, userId: String)



    @Query("UPDATE review_table SET item_comments = :commentsJson WHERE id = :itemId")
    suspend fun updateComments(itemId: kotlin.String, commentsJson: String)


//    @Query("SELECT * FROM review_table WHERE :selectedCategories IS NULL OR ',' || REPLACE(item_category, ', ', ',') || ',' LIKE '%,' || :selectedCategories || ',%'")
//    fun getItemsByCategory(selectedCategories: String?): Flow<List<Item>>

    @Query("""
    SELECT * FROM review_table 
    WHERE (',' || item_category || ',' LIKE '%,' || :categoryId || ',%')
""")
    fun getItemsByCategory(categoryId: String): Flow<List<Item>>


    @Query("DELETE FROM review_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM review_table WHERE item_rating >= :selectedRating AND item_price <= :selectedMaxPrice")
    fun getFilteredItems(selectedRating: Int, selectedMaxPrice: Double): Flow<List<Item>>







}
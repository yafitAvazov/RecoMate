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
    fun deleteItem(vararg item: Item)

    @Update

    fun updateItem(item: Item)

    @Query("SELECT * FROM review_table WHERE id = :itemId")
    fun getItemById(itemId: Int): Flow<Item>

    @Query("SELECT * FROM review_table ORDER BY id DESC")
    fun getItems(): Flow<List<Item>> // ✅ צריך להחזיר Flow<List<Item>>


    @Query("SELECT * FROM review_table WHERE id LIKE :id")
    fun getItem(id:Int) : LiveData<Item>



    @Query("UPDATE review_table SET item_comments = :commentsJson WHERE id = :itemId")
    suspend fun updateComments(itemId: Int, commentsJson: String)


    @Query("SELECT * FROM review_table WHERE :selectedCategories IS NULL OR ',' || REPLACE(item_category, ', ', ',') || ',' LIKE '%,' || :selectedCategories || ',%'")
    fun getItemsByCategory(selectedCategories: String?): Flow<List<Item>>


    @Query( "DELETE FROM review_table")
    fun deleteAll()



    @Query("""
    SELECT * FROM review_table 
    WHERE item_rating >= :selectedRating 
    AND item_price <= :selectedMinPrice
""")
    suspend fun getFilteredItems(
        selectedRating: Int,
        selectedMinPrice: Double
    ): List<Item>






}
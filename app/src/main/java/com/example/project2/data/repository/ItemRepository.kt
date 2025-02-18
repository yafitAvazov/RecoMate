package com.example.project2.data.repository

import com.example.project2.data.local_db.ItemDao
import com.example.project2.data.model.Item
import com.example.project2.utils.Resource
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(private val itemDao: ItemDao) {

    fun getItems(): Flow<List<Item>> = itemDao.getItems() // ✅ Flow במקום ערך סינכרוני


    suspend fun addItem(item: Item) {
        itemDao.addItem(item)
    }

    fun updateItem(item: Item) {
        itemDao.updateItem(item)
    }

    suspend fun deleteItem(item: Item) {
        withContext(Dispatchers.IO) {
            itemDao.deleteItem(item)
        }
    }


    fun getItemById(itemId: Int): Flow<Item?> = itemDao.getItemById(itemId)


    fun deleteAll() {
        itemDao.deleteAll()
    }

    suspend fun updateItemComments(itemId: Int, comments: List<String>) {
        withContext(Dispatchers.IO) {
            val commentsJson = com.google.gson.Gson().toJson(comments)
            itemDao.updateComments(itemId, commentsJson)
        }
    }

    fun getFilteredItems(selectedRating: Int, selectedMaxPrice: Double) =
        flow {
            emit(Resource.loading()) // Send loading state
            try {
                val result = itemDao.getFilteredItems(selectedRating, selectedMaxPrice)
                emit(Resource.success(result)) // Send the filtered data
            } catch (e: Exception) {
                emit(Resource.error("Error fetching filtered items: ${e.message}")) // Send the error
            }
        }.flowOn(Dispatchers.IO)
}

package com.example.project2.data.repository.local

import com.example.project2.data.local_db.ItemDao
import com.example.project2.data.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryLocal @Inject constructor(private val itemDao: ItemDao) {

    fun getItems(): Flow<List<Item>> = itemDao.getItems()

    fun getItemsByCategory(category: String): Flow<List<Item>> = itemDao.getItemsByCategory(category)


    suspend fun addItem(item: Item) = withContext(Dispatchers.IO) {
        itemDao.addItem(item)
    }

    suspend fun updateItem(item: Item) = withContext(Dispatchers.IO) {
        itemDao.updateItem(item)
    }

    suspend fun deleteItem(item: Item) = withContext(Dispatchers.IO) {
        itemDao.deleteItem(item)
    }


    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        itemDao.deleteAll()
    }

    fun getItemById(itemId: String): Flow<Item?> = itemDao.getItemById(itemId)

    suspend fun updateItemComments(itemId: String, commentsJson: String) = withContext(Dispatchers.IO) {
        itemDao.updateComments(itemId, commentsJson)
    }





    fun getUserFavorites(userId: String): Flow<List<Item>> = itemDao.getUserFavorites(userId)

    suspend fun getFilteredItems(selectedRating: Int, selectedMaxPrice: Double): Flow<List<Item>> {
        return itemDao.getFilteredItems(selectedRating, selectedMaxPrice)
    }
    suspend fun updateLikeStatus(itemId: String, isLiked: Boolean) = withContext(Dispatchers.IO) {
        itemDao.updateLikeStatus(itemId, isLiked)
    }



//    suspend fun addFavorite(itemId: Int, userId: String) = withContext(Dispatchers.IO) {
//        itemDao.addFavorite(itemId, userId)
//    }
//
//    suspend fun removeFavorite(itemId: Int, userId: String) = withContext(Dispatchers.IO) {
//        itemDao.removeFavorite(itemId, userId)
//    }

}
package com.example.project2.data.repository

import com.example.project2.data.repository.local.ItemRepositoryLocal
import com.example.project2.data.repository.firebaseImpl.ItemRepositoryFirebase
import com.example.project2.data.model.Item
import com.example.project2.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val itemRepositoryLocal: ItemRepositoryLocal,
    private val itemRepositoryFirebase: ItemRepositoryFirebase
) {

//    fun getItems(): Flow<List<Item>> = flow {
//        val localItems = itemRepositoryLocal.getItems().firstOrNull()
//        if (localItems.isNullOrEmpty()) {
//            itemRepositoryFirebase.getItems().collect { firebaseItems ->
//                emit(firebaseItems)
//                saveItemsLocally(firebaseItems)
//            }
//        } else {
//            emit(localItems)
//        }
//    }.flowOn(Dispatchers.IO)
fun getItems(): Flow<List<Item>> = itemRepositoryFirebase.getItems()


    fun getUserItems(): Flow<List<Item>> = itemRepositoryFirebase.getUserItems()

    suspend fun addItem(item: Item) {
        itemRepositoryFirebase.addItem(item)
        itemRepositoryLocal.addItem(item)
    }

    suspend fun updateItem(item: Item) {
        withContext(Dispatchers.IO) { // ✅ רץ ברקע
            //itemDao.updateItem(item)
            itemRepositoryFirebase.updateItem(item)
            itemRepositoryLocal.updateItem(item)

        }
    }


    suspend fun deleteItem(item: Item) {
        withContext(Dispatchers.IO) {
            try {
                itemRepositoryFirebase.deleteItem(item) // 🔥 מוחק מפיירבייס
                itemRepositoryLocal.deleteItem(item)   // 🔥 מוחק מה-Local DB
            } catch (e: Exception) {
                println("❌ Error deleting item: ${e.message}")
            }
        }
    }





    suspend fun deleteAllUserItems() {
        itemRepositoryFirebase.deleteAllUserItems()
        itemRepositoryLocal.deleteAll()
    }
    suspend fun updateItemComments(itemId: Int, comments: List<String>) {
        withContext(Dispatchers.IO) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                println("⚠️ Error: User not logged in")
                return@withContext
            }

            // עדכון ב-Firebase (רק אם המשתמש הוא הבעלים של ההמלצה)
            val firebaseItem = itemRepositoryFirebase.getItemById(itemId).firstOrNull()
            if (firebaseItem != null && firebaseItem.userId == userId) {
                itemRepositoryFirebase.updateItemComments(itemId, comments)
            }

            // עדכון במסד הנתונים המקומי (Room)
            val commentsJson = com.google.gson.Gson().toJson(comments)
            itemRepositoryLocal.updateItemComments(itemId, commentsJson)
        }
    }



    fun getItemById(itemId: Int): Flow<Item?> = flow {
        val localItem = itemRepositoryLocal.getItemById(itemId).firstOrNull()
        if (localItem != null) {
            emit(localItem)
        } else {
            itemRepositoryFirebase.getItemById(itemId).collect { firebaseItem ->
                emit(firebaseItem)
            }
        }
    }.flowOn(Dispatchers.IO)


    fun getFilteredItems(selectedRating: Int, selectedMaxPrice: Double) =
        flow {
            emit(Resource.loading()) // Send loading state
            try {
                val result = itemDao.getFilteredItems(selectedRating, selectedMaxPrice)
                emit(Resource.success(result)) // Send the filtered data
            } catch (e: Exception) {
                emit(Resource.error("Error fetching filtered items: ${e.message}")) // Send the error
            }
        }.flowOn(Dispatchers.IO) // גורם לקוד לרוץ על `Dispatchers.IO`
    fun getItemsByCategory(selectedCategories: String): Flow<List<Item>> {
        return itemDao.getItemsByCategory(selectedCategories)
    }

    suspend fun updateLikeStatus(itemId: Int, isLiked: Boolean) {
        withContext(Dispatchers.IO) {
            itemRepositoryFirebase.updateLikeStatus(itemId, isLiked)
            itemRepositoryLocal.updateLikeStatus(itemId, isLiked)
        }
    }


    fun getUserFavorites(): Flow<List<Item>> = flow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@flow
        emitAll(itemRepositoryLocal.getUserFavorites(userId)) // 🔥 מעביר את ה-userId ל- ItemRepositoryLocal
    }.flowOn(Dispatchers.IO)




//    suspend fun addFavorite(itemId: Int) {
//        withContext(Dispatchers.IO) {
//            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext
//            itemRepositoryFirebase.addFavorite(itemId, userId)
//            itemRepositoryLocal.addFavorite(itemId, userId)
//        }
//    }
//
//    suspend fun removeFavorite(itemId: Int) {
//        withContext(Dispatchers.IO) {
//            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext
//            itemRepositoryFirebase.removeFavorite(itemId, userId)
//            itemRepositoryLocal.removeFavorite(itemId, userId)
//        }
//    }



    private suspend fun saveItemsLocally(items: List<Item>) {
        withContext(Dispatchers.IO) {
            itemRepositoryLocal.deleteAll()
            items.forEach { itemRepositoryLocal.addItem(it) }
        }
    }
}

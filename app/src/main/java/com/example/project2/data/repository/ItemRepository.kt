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

    suspend fun updateItemComments(itemId: String, comments: List<String>) {
        withContext(Dispatchers.IO) {
            // עדכון ב-Firebase
            itemRepositoryFirebase.updateItemComments(itemId, comments)

            // עדכון במסד הנתונים המקומי (Room)
            val commentsJson = com.google.gson.Gson().toJson(comments)
            itemRepositoryLocal.updateItemComments(itemId, commentsJson)
        }
    }
    suspend fun getUsernameByUserId(userId: String): String? {
        return itemRepositoryFirebase.getUsernameByUserId(userId)
    }

//    // ✅ עדכון תגובות לפריט בפיירבייס ובמקומית
//    suspend fun updateItemComments(itemId: String, comments: List<String>) {
//        itemRepositoryFirebase.updateItemComments(itemId, comments)
//        itemRepositoryLocal.updateItemComments(itemId, comments)
//    }

    fun getItemById(itemId: String): Flow<Item?> = flow {
        val localItem = itemRepositoryLocal.getItemById(itemId).firstOrNull()
        if (localItem != null) {
            emit(localItem)
        } else {
            itemRepositoryFirebase.getItemById(itemId).collect { firebaseItem ->
                emit(firebaseItem)
            }
        }
    }.flowOn(Dispatchers.IO)


    fun getFilteredItems(selectedRating: Int, selectedMaxPrice: Double): Flow<List<Item>> = flow {
        val firebaseItems = itemRepositoryFirebase.getItems() // ✅ Fetch all items from Firebase first
            .firstOrNull()
            ?.filter { item ->
                item.rating >= selectedRating && item.price <= selectedMaxPrice // ✅ Apply filtering
            } ?: emptyList()

        if (firebaseItems.isEmpty()) {
            println("🔥 DEBUG: No items match the filter criteria in Firestore!")
        } else {
            println("🔥 DEBUG: ${firebaseItems.size} items found after filtering!")
        }

        emit(firebaseItems) // ✅ Emit filtered list from Firestore
    }.flowOn(Dispatchers.IO)




    fun getItemsByCategory(selectedCategories: String): Flow<List<Item>> =
        flow {
            val localItems = itemRepositoryLocal.getItemsByCategory(selectedCategories).firstOrNull()
            if (!localItems.isNullOrEmpty()) {
                emit(localItems)
            } else {
                itemRepositoryFirebase.getItemsByCategory(selectedCategories).collect { firebaseItems ->
                    emit(firebaseItems)
                    saveItemsLocally(firebaseItems) // Save fetched items in local DB
                }
            }
        }.flowOn(Dispatchers.IO)


    suspend fun updateLikeStatus(itemId: String, isLiked: Boolean) {
        withContext(Dispatchers.IO) {
            itemRepositoryFirebase.updateLikeStatus(itemId, isLiked)
            itemRepositoryLocal.updateLikeStatus(itemId, isLiked)
        }
    }


    fun getUserFavorites(): Flow<List<Item>> = flow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@flow

        // ✅ Fetch both local and Firebase favorites
        val localFavorites = itemRepositoryLocal.getUserFavorites(userId).firstOrNull() ?: emptyList()
        val firebaseFavorites = itemRepositoryFirebase.getUserFavorites().firstOrNull() ?: emptyList()

        val combinedFavorites = (localFavorites + firebaseFavorites).distinctBy { it.id }

        if (combinedFavorites.isEmpty()) {
            println("🔥 DEBUG: No favorite items found in Firebase or Local DB!")
        } else {
            println("🔥 DEBUG: ${combinedFavorites.size} favorite items found!")
        }

        emit(combinedFavorites) // ✅ Ensures all liked items appear
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

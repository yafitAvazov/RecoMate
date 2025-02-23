package com.example.project2.data.repository

import com.example.project2.data.repository.local.ItemRepositoryLocal
import com.example.project2.data.repository.firebaseImpl.ItemRepositoryFirebase
import com.example.project2.data.model.Item
import com.example.project2.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

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

    fun getTopLikedItems(): Flow<List<Item>> = itemRepositoryFirebase.getTopLikedItems()



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






    suspend fun updateLikeStatus(itemId: String, userId: String) {
        withContext(Dispatchers.IO) {
            val item = itemRepositoryLocal.getItemById(itemId).firstOrNull()
                ?: itemRepositoryFirebase.getItemById(itemId).firstOrNull()
                ?: return@withContext

            val isAlreadyLiked = item.likedBy.contains(userId)
            val updatedLikedBy = if (isAlreadyLiked) item.likedBy - userId else item.likedBy + userId

            // ✅ Only update if there's a change
            if (updatedLikedBy != item.likedBy) {
                val likedByJson = Gson().toJson(updatedLikedBy)

                itemRepositoryFirebase.updateLikeStatus(itemId, updatedLikedBy)
                itemRepositoryLocal.updateLikeStatus(itemId, likedByJson)
            }
        }
    }







    fun getUserFavorites(): Flow<List<Item>> = callbackFlow {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            close(Exception("User not logged in"))
            return@callbackFlow
        }

        val itemRef = FirebaseFirestore.getInstance().collection("items") // ✅ Ensure Firestore reference is correct

        val listener = itemRef
            .whereArrayContains("likedBy", currentUser.uid) // ✅ Fetch only items liked by the logged-in user
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("❌ Firestore Error: ${e.message}")
                    close(e)
                    return@addSnapshotListener
                }

                val items = snapshot?.toObjects(Item::class.java) ?: emptyList()

                if (items.isEmpty()) {
                    println("🔥 DEBUG: No liked items found for user ${currentUser.uid} in Firestore!")
                } else {
                    println("🔥 DEBUG: ${items.size} liked items found for user ${currentUser.uid}!")
                }

                trySend(items).isSuccess
            }

        awaitClose { listener.remove() }
    }









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

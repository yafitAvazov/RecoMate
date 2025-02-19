package com.example.project2.data.repository.firebaseImpl

import com.example.project2.data.model.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryFirebase @Inject constructor() {

    private val itemRef = FirebaseFirestore.getInstance().collection("items")
    private val auth = FirebaseAuth.getInstance()

//    fun getItems(): Flow<List<Item>> = callbackFlow {
//        val listener = itemRef.addSnapshotListener { snapshot, e ->
//            if (snapshot != null) {
//                val items = snapshot.toObjects(Item::class.java)
//                trySend(items)
//            } else {
//                close(e)
//            }
//        }
//        awaitClose { listener.remove() }
//    }
fun getItems(): Flow<List<Item>> = callbackFlow {
    val listener = itemRef.addSnapshotListener { snapshot, _ ->
        val items = snapshot?.toObjects(Item::class.java) ?: emptyList()
        trySend(items)
    }
    awaitClose { listener.remove() }
}

    fun getUserItems(): Flow<List<Item>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            close(Exception("User not logged in"))
            return@callbackFlow
        }

        val listener = itemRef
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val items = snapshot.toObjects(Item::class.java)
                    trySend(items)
                } else {
                    close(e)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addItem(item: Item) = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        val itemId = itemRef.document().id // 🔥 יצירת ID ייחודי בפיירבייס

        val newItem = item.copy(id = itemId, userId = userId) // 🔥 עכשיו ה-ID הוא String

        try {
            itemRef.document(itemId).set(newItem).await()
            println("✅ Item added successfully with ID: $itemId")
        } catch (e: Exception) {
            println("❌ Error adding item: ${e.message}")
        }
    }



    suspend fun updateItem(item: Item) = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        if (item.userId == userId) {
            itemRef.document(item.id).set(item).await()
        }
    }

    suspend fun deleteItem(item: Item) = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext

        try {
            val documentRef = itemRef.document(item.id)

            // 🔥 בדיקת קיום הפריט לפני מחיקה
            val snapshot = documentRef.get().await()
            if (snapshot.exists()) {
                documentRef.delete().await() // ✅ מוחק מה-Firebase
            } else {
                println("❌ Error: Item does not exist in Firestore!")
            }
        } catch (e: Exception) {
            println("❌ Error deleting item: ${e.message}")
        }
    }


    suspend fun updateLikeStatus(itemId: String, isLiked: Boolean) = withContext(Dispatchers.IO) {
        val documentRef = itemRef.document(itemId)

        val snapshot = documentRef.get().await()
        if (!snapshot.exists()) {
            println("❌ Error: Item does not exist in Firestore!")
            return@withContext
        }

        documentRef.update("isLiked", isLiked).await() // 🔥 מעדכן בפיירבייס
    }




    fun getUserFavorites(): Flow<List<Item>> = callbackFlow {
        val listener = itemRef.whereEqualTo("isLiked", true).addSnapshotListener { snapshot, _ ->
            val items = snapshot?.toObjects(Item::class.java) ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

//    suspend fun addFavorite(itemId: Int, userId: String) = withContext(Dispatchers.IO) {
//        updateLikeStatus(itemId, userId, true) // ✅ עדכון במועדפים
//    }
//
//    suspend fun removeFavorite(itemId: Int, userId: String) = withContext(Dispatchers.IO) {
//        updateLikeStatus(itemId, userId, false) // ✅ הסרת מהמועדפים
//    }







    suspend fun deleteAllUserItems() = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        val batch = FirebaseFirestore.getInstance().batch()
        val snapshot = itemRef.whereEqualTo("userId", userId).get().await()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    fun getItemById(itemId: String): Flow<Item?> = callbackFlow {
        val listener = itemRef.document(itemId).addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                val item = snapshot.toObject(Item::class.java)
                trySend(item)
            } else {
                close(e)
            }
        }
        awaitClose { listener.remove() }
    }
    suspend fun updateItemComments(itemId: String, comments: List<String>) = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        val itemSnapshot = itemRef.document(itemId).get().await()

        // בדיקה האם הפריט קיים והאם המשתמש המחובר הוא הבעלים
        val item = itemSnapshot.toObject(Item::class.java)
        if (item != null && item.userId == userId) {
            val commentsJson = com.google.gson.Gson().toJson(comments)
            itemRef.document(itemId).update("item_comments", commentsJson).await()
        } else {
            println("⚠️ Error: User is not authorized to update this item's comments")
        }
    }
    fun getItemsByCategory(category: String): Flow<List<Item>> = callbackFlow {
        val listener = itemRef.whereEqualTo("category", category)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val items = snapshot.toObjects(Item::class.java)
                    trySend(items).isSuccess
                } else {
                    close(e)
                }
            }
        awaitClose { listener.remove() }
    }


}

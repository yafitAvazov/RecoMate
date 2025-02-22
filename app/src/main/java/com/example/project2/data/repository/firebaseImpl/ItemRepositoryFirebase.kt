package com.example.project2.data.repository.firebaseImpl

import android.app.Application
import android.widget.Toast
import com.example.project2.data.model.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.internal.Contexts.getApplication
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

    fun getTopLikedItems(): Flow<List<Item>> = callbackFlow {
        val listener = itemRef
            .orderBy("likedBy", com.google.firebase.firestore.Query.Direction.DESCENDING) // Order by most liked
            .limit(5) // Limit to top 5
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(Item::class.java) ?: emptyList()
                trySend(items).isSuccess
            }

        awaitClose { listener.remove() }
    }



    suspend fun updateLikeStatus(itemId: String, likedBy: List<String>) {
        val itemRef = FirebaseFirestore.getInstance().collection("items").document(itemId)

        try {
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                transaction.update(itemRef, "likedBy", likedBy)
            }.await()
        } catch (e: Exception) {
            println("❌ Error updating like status in Firebase: ${e.message}")
        }
    }












    fun getUserFavorites(): Flow<List<Item>> = callbackFlow {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            close(Exception("User not logged in"))
            return@callbackFlow
        }

        val itemRef = FirebaseFirestore.getInstance().collection("items")

        val listener = itemRef
            .whereArrayContains("likedBy", currentUser.uid) // ✅ Only fetch items the user liked
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("❌ Firestore Error: ${e.message}")
                    close(e)
                    return@addSnapshotListener
                }

                val items = snapshot?.toObjects(Item::class.java) ?: emptyList()
                println("🔥 DEBUG: ${items.size} liked items found for user ${currentUser.uid}!")

                trySend(items).isSuccess
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
        val documentRef = itemRef.document(itemId)

        // 🔥 ודא שקיים פריט עם ה-ID
        val itemSnapshot = documentRef.get().await()
        if (!itemSnapshot.exists()) {
            println("⚠️ Error: Item does not exist in Firestore")
            return@withContext
        }

        // 🔥 שמירת התגובות כ-Array בפיירבייס
        documentRef.update("comments", comments).await()
        println("✅ Comments updated successfully")
    }

    suspend fun getUsernameByUserId(userId: String): String? = withContext(Dispatchers.IO) {
        try {
            val userSnapshot = FirebaseFirestore.getInstance()
                .collection("users") // 🔥 ודאי שהשם מדויק - בדיוק כמו ב-Firestore
                .document(userId)
                .get()
                .await()

            // 🔥 בדיקת הנתונים שנשלפו מה-Database
            println("🔥 DEBUG: Document data: ${userSnapshot.data}")
            val username = userSnapshot.getString("name")
            println("🔥 DEBUG: Username: $username")

            return@withContext username
        } catch (e: Exception) {
            println("❌ Error fetching username: ${e.message}")
            return@withContext null
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

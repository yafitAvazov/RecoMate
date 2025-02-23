package com.example.project2.ui.recommendation_detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2.data.model.Item
import com.example.project2.data.repository.ItemRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationDetailViewModel @Inject constructor(
    private val repository: ItemRepository,
) : ViewModel() {

    private val _chosenItem = MutableLiveData<Item?>()
    val chosenItem: MutableLiveData<Item?> get() = _chosenItem

    val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid
    val commentsMap = MutableLiveData<MutableMap<String, MutableList<String>>>().apply {
        value = mutableMapOf()
    }


    fun fetchItemById(itemId: String) {
        viewModelScope.launch {
            try {
                repository.getItemById(itemId).collect { item ->
                    if (item != null) {
                        _chosenItem.value = item
                        Log.d("ViewModel", "Item fetched successfully: ${item.title}")
                    } else {
                        Log.e("ViewModel", "Failed to fetch item, item is null")
                    }
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching item by ID", e)
            }
        }
    }

    fun getUsername(): LiveData<String?> {
        val liveData = MutableLiveData<String?>()
        viewModelScope.launch(Dispatchers.IO) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // 🔥 בדיקת userId
            println("🔥 DEBUG: Current User ID: $userId")

            if (userId != null) {
                val username = repository.getUsernameByUserId(userId)
                liveData.postValue(username)
            } else {
                liveData.postValue(null)
            }
        }
        return liveData
    }


//    fun getCurrentUserName(): String? {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        return currentUser?.displayName ?: currentUser?.email // מציג שם או דוא"ל אם אין שם
//    }



    fun updateItemComments(itemId: String, newComments: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItemComments(itemId, newComments)
        refreshItemComments(itemId) // 🔥 טוען מחדש את התגובות מהפיירבייס
        }
    }


    fun refreshItemComments(itemId: String) {
        viewModelScope.launch {
            repository.getItemById(itemId).collect { item ->
                _chosenItem.postValue(item)
            }
        }
    }





    fun setItem(item: Item) {
        _chosenItem.value = item
    }

    fun updateItem(item: Item) {
        if (item.userId == currentUserId) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateItem(item)
                _chosenItem.postValue(item)
            }
        }
    }

    fun getItemById(itemId: String): LiveData<Item?> {
        val liveData = MutableLiveData<Item?>()
        viewModelScope.launch {
            repository.getItemById(itemId).collect { item ->
                liveData.postValue(item)
            }
        }
        return liveData
    }






}

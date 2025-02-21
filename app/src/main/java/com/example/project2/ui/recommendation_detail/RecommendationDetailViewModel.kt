package com.example.project2.ui.recommendation_detail

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
    private val repository: ItemRepository
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
                val item = repository.getItemById(itemId).firstOrNull()
                _chosenItem.postValue(item)
            } catch (e: Exception) {
                _chosenItem.postValue(null)
            }
        }
    }



    fun updateItemComments(item: Item, newComments: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItemComments(item.id, newComments)
            fetchItemById(item.id) // ✅ מעביר String
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

    fun getItemById(itemId: String) {
        viewModelScope.launch {
            repository.getItemById(itemId).collect { item ->
                _chosenItem.postValue(item)
            }
        }
    }


}

package com.example.project2.ui.recommendation_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2.data.model.Item
import com.example.project2.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationDetailViewModel @Inject constructor(
    private val repository: ItemRepository
) : ViewModel() {

    private val _chosenItem = MutableLiveData<Item?>()
    val chosenItem: MutableLiveData<Item?> get() = _chosenItem

    val commentsMap = MutableLiveData<MutableMap<String, MutableList<String>>>().apply {
        value = mutableMapOf()
    }

    /**
     * בחירת פריט להצגה
     */
    fun setItem(item: Item) {
        _chosenItem.value = item
    }

    /**
     * עדכון פריט קיים
     */
    fun updateItem(item: Item) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }
    fun getItemById(itemId: Int) {
        viewModelScope.launch {
            repository.getItemById(itemId).collect { item ->
                _chosenItem.postValue(item)
            }
        }
    }

    /**
     * עדכון תגובות לפריט
     */
    fun updateItemComments(item: Item, newComments: List<String>) {
        viewModelScope.launch(Dispatchers.IO) { // ✅ הפעלת הקוד ב-IO Thread
            repository.updateItemComments(item.id, newComments)
            val updatedItem = item.copy(comments = newComments)
            _chosenItem.postValue(updatedItem)
        }
    }

}

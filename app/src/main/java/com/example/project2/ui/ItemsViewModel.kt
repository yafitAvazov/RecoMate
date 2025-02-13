package com.example.project2.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.project2.data.model.Item
import com.example.project2.data.repository.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ItemsViewModel (application: Application) : AndroidViewModel(application){
    private val repository = ItemRepository(application)
    val items : LiveData<List<Item>>? = repository.getItems()

    private val _chosenItem = MutableLiveData<Item>()
    val chosenItem: LiveData<Item> get() = _chosenItem

    private val _filteredItems = MutableLiveData<List<Item>>()
    val filteredItems: LiveData<List<Item>> get() = _filteredItems

    val commentsMap = MutableLiveData<MutableMap<String, MutableList<String>>>().apply {
        value = mutableMapOf()
    }
    fun updateItemComments(item: Item, newComments: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItemComments(item.id, newComments)

            // ✅ שומר את התגובות גם ב-LiveData כדי שהן לא ייעלמו לאחר רענון
            val updatedItem = item.copy(comments = newComments)
            _chosenItem.postValue(updatedItem)
        }
    }




    fun setItem(item: Item) {
        _chosenItem.value = item
    }

    fun addItem(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addItem(item)

            // קבלת הרשימה הנוכחית והוספת הפריט החדש לראש הרשימה
            val updatedList = mutableListOf<Item>().apply {
                add(item) // הוספת הפריט לראש הרשימה
                addAll(items?.value ?: emptyList()) // הוספת שאר הפריטים
            }

            _filteredItems.postValue(updatedList) // עדכון הרשימה החדשה
        }
    }

    /** ✅ מחיקת פריט בודד */
    fun deleteItem(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(item)
        }
    }
    /** ✅ עדכון פריט קיים */
    fun updateItem(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItem(item)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
            _filteredItems.postValue(emptyList()) // 🟢 עדכון LiveData כדי להפעיל את ה-Observer
        }
    }

    fun setFilteredItems(items: List<Item>) {
        _filteredItems.value = items
    }

    suspend fun getFilteredItems(selectedCategories: String?, selectedRating: Int, selectedMinPrice: Double): List<Item> {
        return repository.getFilteredItems(selectedCategories, selectedRating, selectedMinPrice)
    }


}
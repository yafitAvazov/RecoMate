package com.example.project2.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.project2.data.model.Item
import com.example.project2.data.repository.ItemRepository
import kotlinx.coroutines.launch

class ItemsViewModel (application: Application) : AndroidViewModel(application){
    private val repository = ItemRepository(application)
    val items : LiveData<List<Item>>? = repository.getItems()

    private val _chosenItem = MutableLiveData<Item>()
    val chosenItem: LiveData<Item> get() = _chosenItem

    private val _filteredItems = MutableLiveData<List<Item>>()
    val filteredItems: LiveData<List<Item>> get() = _filteredItems


    fun setItem(item: Item) {
        _chosenItem.value = item
    }

    fun addItem(item: Item) {
        repository.addItem(item)
    }
    fun deleteItem(item: Item) {
        repository.deleteItem(item)
    }
    fun updateItem(item: Item) {
        viewModelScope.launch {
            repository.updateItem(item) // מעדכן את הפריט ב-DAO
        }
    }
    fun deleteAll() {
        repository.deleteAll()

    }
    fun setFilteredItems(items: List<Item>) {
        _filteredItems.value = items
    }
}
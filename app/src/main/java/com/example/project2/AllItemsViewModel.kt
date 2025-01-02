package com.example.project2

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class AllItemsViewModel : ViewModel() {

    private val _items = MutableLiveData<List<Item>>(emptyList()) // נתונים פרטיים
    val items: LiveData<List<Item>> get() = _items // חשיפת הנתונים כ-LiveData

    fun addItem(item: Item) {
        val currentItems = _items.value.orEmpty().toMutableList()
        currentItems.add(item)
        _items.value = currentItems
        Log.d("ViewModel", "Added item: $item") // הוספת לוג לבדיקה
    }

    fun removeItem(index: Int) {
        val currentItems = _items.value?.toMutableList() ?: return
        currentItems.removeAt(index)
        _items.value = currentItems
    }
}

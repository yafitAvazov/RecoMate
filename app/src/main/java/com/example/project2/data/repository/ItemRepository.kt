package com.example.project2.data.repository

import android.app.Application
import androidx.lifecycle.liveData
import com.example.project2.data.local_db.ItemDao
import com.example.project2.data.local_db.ItemDataBase
import com.example.project2.data.model.Item
import kotlinx.coroutines.Dispatchers

class ItemRepository (application: Application){
    private var itemDao:ItemDao?
    init {
        val db = ItemDataBase.getDatabase(application.applicationContext)
        itemDao = db?.itemsDao()
    }
    fun getItems() = itemDao?.getItems()

    fun addItem(item: Item) {
        itemDao?.addItem(item)
    }
    fun updateItem(item: Item) {
        itemDao?.updateItem(item)
    }

    fun deleteItem(item: Item) {
        itemDao?.deleteItem(item)
    }
    fun getItem(id: Int) = itemDao?.getItem(id)

    fun deleteAll() {
        itemDao?.deleteAll()
    }
    suspend fun getFilteredItems(selectedCategories: String?, selectedRating: Int, selectedMinPrice: Double): List<Item> {
        return itemDao?.getFilteredItems(selectedCategories, selectedRating, selectedMinPrice) ?: emptyList()
    }




}
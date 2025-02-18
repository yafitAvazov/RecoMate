package com.example.project2.ui.all_recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2.data.model.Item
import com.example.project2.data.repository.ItemRepository
import com.example.project2.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationListViewModel @Inject constructor(
    private val repository: ItemRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<Item>>(emptyList()) // ✅ ודאי שהמשתנה מוגדר
    val items: StateFlow<List<Item>> get() = _items.asStateFlow()


    private val _filteredItems = MutableStateFlow<Resource<List<Item>>>(Resource.loading(emptyList()))
    val filteredItems: StateFlow<Resource<List<Item>>> = _filteredItems.asStateFlow()

    init {
        fetchItems()
    }

    fun fetchItems() {
        viewModelScope.launch {
            repository.getItems()
                .collect { itemList ->
                    _items.value = itemList // ✅ עדכון ה-Flow בצורה נכונה
                }
        }
    }



    fun fetchFilteredItems(selectedRating: Int, selectedMaxPrice: Double) {
        viewModelScope.launch {
            repository.getFilteredItems(selectedRating, selectedMaxPrice)
                .collect { resource ->
                    _items.value = resource.data ?: emptyList() // ✅ Ensure the filtered list is reflected here
                }
        }
    }


    fun fetchSortedItems(sortBy: String) {
        viewModelScope.launch {
            val sortedList = when (sortBy) {
                "price_asc" -> _items.value.sortedBy { it.price }
                "price_desc" -> _items.value.sortedByDescending { it.price }
                "stars_desc" -> _items.value.sortedByDescending { it.rating }
                else -> _items.value
            }
            _items.value = sortedList
        }
    }






    fun addItem(item: Item) {
        viewModelScope.launch {
            repository.addItem(item)
            fetchItems()
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(item)
        }
    }


    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
            _items.value = emptyList() // ✅ עדכון הרשימה ל-UI לאחר המחיקה
        }
    }

}

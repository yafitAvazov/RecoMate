package com.example.project2.ui.all_recommendation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2.data.model.Item
import com.example.project2.data.repository.AuthRepository
import com.example.project2.data.repository.ItemRepository
import com.example.project2.utils.Resource
import com.google.firebase.auth.FirebaseAuth

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RecommendationListViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    //    private val _items = MutableStateFlow<List<Item>>(emptyList())
//    val items: StateFlow<List<Item>> get() = _items.asStateFlow()
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _userItems = MutableStateFlow<List<Item>>(emptyList())
    val userItems: StateFlow<List<Item>> get() = _userItems.asStateFlow()




    private val _filteredItems = MutableStateFlow<Resource<List<Item>>>(Resource.loading(emptyList()))
    val filteredItems: StateFlow<Resource<List<Item>>> = _filteredItems.asStateFlow()

    private val _userFavorites = MutableStateFlow<List<Item>>(emptyList())
    val userFavorites: StateFlow<List<Item>> = _userFavorites.asStateFlow()

    init {
        fetchItems()
        fetchUserItems()
//        fetchUserFavorites()

    }

    fun signOut() {
        authRepository.logout()
        _userItems.value = emptyList() // ✅ מנקה את הרשימה כדי למנוע טעינת נתונים אחרי ההתנתקות
    }


    fun fetchItems() {
        viewModelScope.launch {
            repository.getItems().collectLatest { newItems ->
                _items.emit(newItems) // ✅ Emits new data so UI updates
            }
        }
    }




    fun fetchFilteredItems(selectedRating: Int, selectedMaxPrice: Double) {
        viewModelScope.launch {
            repository.getFilteredItems(selectedRating, selectedMaxPrice) // ✅ Collect the Flow
                .collect { resource: Resource<List<Item>> -> // ✅ Explicitly define the type
                    _filteredItems.value = resource // ✅ Assign Resource<List<Item>> properly
                    _items.value = resource.data ?: emptyList() // ✅ Extract List<Item>
                }
        }
    }



    fun fetchUserItems() {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                _userItems.value = emptyList() // ✅ אם המשתמש התנתק, מחזירים רשימה ריקה
                return@launch
            }
            repository.getUserItems().collect { itemList ->
                _userItems.value = itemList
            }
        }
    }
    fun updateLikeStatus(item: Item, isLiked: Boolean) {
        viewModelScope.launch {
            repository.updateLikeStatus(item.id, isLiked)
            fetchUserFavorites() // 🔥 עדכון המועדפים לאחר שינוי
        }
    }

    // ✅ פונקציה להבאת ה- User ID
    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun addItem(item: Item) {
        viewModelScope.launch {
            repository.addItem(item)
            fetchItems()
            fetchUserItems()
        }
    }

    fun updateItem(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItem(item)
            fetchItems()
            fetchUserItems()
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(item) // 🔥 מוחק מה-Firebase ומה-Local DB

            withContext(Dispatchers.Main) {
                _items.value = _items.value.filterNot { it.id == item.id } // 🔥 מוחק מרשימת כל ההמלצות
                _userItems.value = _userItems.value.filterNot { it.id == item.id } // 🔥 מוחק מרשימת ההמלצות שלי
            }

        }
    }


//    fun updateLikeStatus(item: Item) {
//        viewModelScope.launch {
//            repository.updateLikeStatus(item.id, item.isLiked)
//        }
//    }

    fun deleteAllUserItems() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllUserItems()
            _userItems.value = emptyList()
        }
    }
    fun fetchUserFavorites() {
        viewModelScope.launch {
            repository.getUserFavorites().collect { _userFavorites.value = it }
        }
    }
    fun updateLikeStatus(itemId: String, isLiked: Boolean) {
        viewModelScope.launch {
            repository.updateLikeStatus(itemId, isLiked) // 🔥 לא צריך המרה!
            fetchUserFavorites()
        }
    }

    fun updateLikeStatus(itemId: Int, isLiked: Boolean) {
        viewModelScope.launch {
            repository.updateLikeStatus(itemId.toString(), isLiked)
            fetchUserFavorites() // 🔥 מרענן את רשימת המועדפים
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
    fun fetchItemsByCategory(category: String) {
        viewModelScope.launch {
            repository.getItemsByCategory(category).collect { itemList ->
                _items.value = itemList
            }
        }
    }
    fun fetchItemById(itemId: String) {
        viewModelScope.launch {
            try {
                val item = repository.getItemById(itemId).firstOrNull()
                _items.value = listOfNotNull(item) // ✅ מעדכן את ה-StateFlow
            } catch (e: Exception) {
                _items.value = emptyList() // ✅ במקרה של שגיאה מחזיר רשימה ריקה
            }
        }
    }

    fun updateItemComments(item: Item, newComments: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItemComments(item.id, newComments)
            fetchItemById(item.id) // ✅ מעביר String
        }
    }







//    fun addFavorite(itemId: Int) { // ✅ מקבל רק את ה-ID
//        viewModelScope.launch {
//            repository.addFavorite(itemId)
//        }
//    }
//
//    fun removeFavorite(itemId: Int) { // ✅ מקבל רק את ה-ID
//        viewModelScope.launch {
//            repository.removeFavorite(itemId)
//        }
//    }


}

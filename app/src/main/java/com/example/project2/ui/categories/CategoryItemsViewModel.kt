package com.example.project2.ui.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2.data.model.Item
import com.example.project2.data.repository.AuthRepository
import com.example.project2.data.repository.ItemRepository
import com.example.project2.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryItemsViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val authRepository: AuthRepository
) :

    ViewModel() {
    private val _userItems = MutableStateFlow<List<Item>>(emptyList())
    val userItems: StateFlow<List<Item>> get() = _userItems.asStateFlow()



    private val _userFavorites = MutableStateFlow<List<Item>>(emptyList())
    val userFavorites: StateFlow<List<Item>> = _userFavorites.asStateFlow()

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> get() = _items

    fun fetchItemsByCategory(category: String) {
        viewModelScope.launch {
            repository.getItemsByCategory(category).collectLatest { itemList ->
                _items.value = itemList
            }
        }
    }
    fun signOut() {
        authRepository.logout()
        _userItems.value = emptyList() // ✅ Clear user items on logout
        _userFavorites.value = emptyList() // ✅ Clear favorites on logout
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

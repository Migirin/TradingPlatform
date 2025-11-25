package com.example.tradingplatform.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradingplatform.data.achievement.AchievementRepository
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.data.items.ItemRepository
import com.example.tradingplatform.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ItemUiState {
    data object Idle : ItemUiState
    data object Loading : ItemUiState
    data class Error(val message: String) : ItemUiState
    data object Success : ItemUiState
}

class ItemViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repo = ItemRepository(application)
    private val authRepo = AuthRepository(application)
    private val achievementRepo = AchievementRepository(application)
    private val _state = MutableStateFlow<ItemUiState>(ItemUiState.Idle)
    val state: StateFlow<ItemUiState> = _state
    
    // 允许外部重置状态（用于导航后清理）
    fun resetState() {
        _state.value = ItemUiState.Idle
    }

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items
    
    // 当前选中的商品（用于详情页）
    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem

    /**
     * 根据ID获取商品
     */
    fun getItemById(itemId: String): Item? {
        return _items.value.find { it.id == itemId }
    }
    
    /**
     * 设置当前选中的商品
     */
    fun setSelectedItem(item: Item) {
        Log.d("ItemViewModel", "设置选中商品: ${item.id} - ${item.title}")
        _selectedItem.value = item
    }
    
    /**
     * 获取当前选中的商品
     */
    fun getSelectedItem(): Item? {
        return _selectedItem.value
    }

    init {
        loadItems()
    }

    fun loadItems() {
        _state.value = ItemUiState.Loading
        viewModelScope.launch {
            try {
                val itemList = repo.listItems()
                _items.value = itemList
                _state.value = ItemUiState.Idle
            } catch (e: Exception) {
                Log.e("ItemViewModel", "加载商品失败", e)
                _items.value = emptyList()
                _state.value = ItemUiState.Error("加载失败: ${e.message ?: "未知错误"}")
            }
        }
    }

    fun postItem(title: String, price: Double, category: String, description: String, story: String, phoneNumber: String, imageUri: Uri?) {
        _state.value = ItemUiState.Loading
        viewModelScope.launch {
            try {
                val item = Item(
                    title = title,
                    price = price,
                    category = category,
                    description = description,
                    story = story,
                    phoneNumber = phoneNumber
                )
                repo.addItem(item, imageUri)
                _state.value = ItemUiState.Success
                // 重新加载列表
                loadItems()
                // 检查成就
                achievementRepo.checkAndGrantAchievements()
            } catch (e: Exception) {
                Log.e("ItemViewModel", "发布商品失败", e)
                _state.value = ItemUiState.Error("发布失败: ${e.message ?: "未知错误"}")
            }
        }
    }

    /**
     * 删除商品（仅开发者模式可用）
     */
    fun deleteItem(itemId: String) {
        _state.value = ItemUiState.Loading
        viewModelScope.launch {
            try {
                // 检查是否为开发者模式（未登录）
                val isDevMode = !authRepo.isLoggedIn()
                
                if (!isDevMode) {
                    Log.w("ItemViewModel", "普通用户不能删除商品")
                    _state.value = ItemUiState.Error("只有开发者模式可以删除商品")
                    return@launch
                }
                
                repo.deleteItem(itemId)
                // 从列表中移除
                _items.value = _items.value.filter { it.id != itemId }
                _state.value = ItemUiState.Success
                // 重新加载列表
                loadItems()
            } catch (e: Exception) {
                Log.e("ItemViewModel", "删除商品失败", e)
                _state.value = ItemUiState.Error("删除失败: ${e.message ?: "未知错误"}")
            }
        }
    }
}


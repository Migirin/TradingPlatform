package com.example.tradingplatform.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradingplatform.data.achievement.AchievementRepository
import com.example.tradingplatform.data.wishlist.ExchangeMatch
import com.example.tradingplatform.data.wishlist.WishlistItem
import com.example.tradingplatform.data.wishlist.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface WishlistUiState {
    data object Idle : WishlistUiState
    data object Loading : WishlistUiState
    data class Error(val message: String) : WishlistUiState
    data object Success : WishlistUiState
}

class WishlistViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repo = WishlistRepository(application)
    private val achievementRepo = AchievementRepository(application)
    
    private val _state = MutableStateFlow<WishlistUiState>(WishlistUiState.Idle)
    val state: StateFlow<WishlistUiState> = _state

    val wishlist: StateFlow<List<WishlistItem>> = try {
        repo.getWishlistFlow()
            .catch { e ->
                Log.e("WishlistViewModel", "愿望清单 Flow 错误", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } catch (e: Exception) {
        Log.e("WishlistViewModel", "初始化愿望清单 Flow 失败", e)
        MutableStateFlow(emptyList())
    }

    private val _matches = MutableStateFlow<List<ExchangeMatch>>(emptyList())
    val matches: StateFlow<List<ExchangeMatch>> = _matches
    
    // 单个愿望清单项的匹配结果 / Match results for a single wishlist item
    private val _singleItemMatches = MutableStateFlow<List<ExchangeMatch>>(emptyList())
    val singleItemMatches: StateFlow<List<ExchangeMatch>> = _singleItemMatches

    init {
        // 初始化时记录日志 / Log on initialization
        Log.d("WishlistViewModel", "WishlistViewModel 初始化")
    }

    fun loadWishlist() {
        // Flow会自动更新，无需手动加载 / Flow will auto-update, no manual loading needed
    }

    fun addWishlistItem(
        title: String,
        category: String,
        minPrice: Double,
        maxPrice: Double,
        targetPrice: Double = 0.0,
        itemId: String = "",
        enablePriceAlert: Boolean = false,
        description: String = ""
    ) {
        _state.value = WishlistUiState.Loading
        viewModelScope.launch {
            try {
                val item = WishlistItem(
                    title = title,
                    category = category,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    targetPrice = targetPrice,
                    itemId = itemId,
                    enablePriceAlert = enablePriceAlert,
                    description = description
                )
                val itemId = repo.addWishlistItem(item)
                Log.d("WishlistViewModel", "愿望清单项添加成功: $itemId, title: ${item.title}")
                _state.value = WishlistUiState.Success
                // Flow会自动更新，无需手动刷新 / Flow will auto-update, no manual refresh needed
                // 检查成就 / Check achievements
                achievementRepo.checkAndGrantAchievements()
            } catch (e: Exception) {
                Log.e("WishlistViewModel", "添加愿望清单项失败", e)
                _state.value = WishlistUiState.Error("添加失败: ${e.message ?: "未知错误"}")
            }
        }
    }

    /**
     * 检查价格变化并发送提醒 / Check price changes and send alerts
     */
    fun checkPriceAlerts() {
        viewModelScope.launch {
            try {
                repo.checkPriceAlerts()
            } catch (e: Exception) {
                Log.e("WishlistViewModel", "检查价格提醒失败", e)
            }
        }
    }

    fun deleteWishlistItem(itemId: String) {
        _state.value = WishlistUiState.Loading
        viewModelScope.launch {
            try {
                repo.deleteWishlistItem(itemId)
                _state.value = WishlistUiState.Success
                // Flow会自动更新，无需手动刷新 / Flow will auto-update, no manual refresh needed
                } catch (e: Exception) {
                Log.e("WishlistViewModel", "删除愿望清单项失败", e)
                _state.value = WishlistUiState.Error("删除失败: ${e.message ?: "未知错误"}")
            }
        }
    }

    fun findMatches(minScore: Double = 30.0, maxResults: Int = 50) {
        _state.value = WishlistUiState.Loading
        viewModelScope.launch {
            try {
                val matchList = repo.findExchangeMatches(minScore, maxResults)
                _matches.value = matchList
                Log.d("WishlistViewModel", "找到 ${matchList.size} 个匹配结果")
                _state.value = WishlistUiState.Success
            } catch (e: Exception) {
                Log.e("WishlistViewModel", "查找匹配失败", e)
                _state.value = WishlistUiState.Error("查找匹配失败: ${e.message ?: "未知错误"}")
            }
        }
    }

    /**
     * 查找单个愿望清单项的匹配结果 / Find match results for a single wishlist item
     */
    fun findMatchesForItem(wishlistItemId: String, minScore: Double = 30.0, maxResults: Int = 20) {
        _state.value = WishlistUiState.Loading
        viewModelScope.launch {
            try {
                val matchList = repo.findMatchesForWishlistItem(wishlistItemId, minScore, maxResults)
                _singleItemMatches.value = matchList
                Log.d("WishlistViewModel", "找到 ${matchList.size} 个匹配结果（针对单个愿望清单项）")
                _state.value = WishlistUiState.Success
            } catch (e: Exception) {
                Log.e("WishlistViewModel", "查找单个愿望清单项匹配失败", e)
                _state.value = WishlistUiState.Error("查找匹配失败: ${e.message ?: "未知错误"}")
            }
        }
    }

    fun resetState() {
        _state.value = WishlistUiState.Idle
    }
}


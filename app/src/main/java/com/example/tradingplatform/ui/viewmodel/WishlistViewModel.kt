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
    
    // 降价提醒消息（用于浮窗显示）/ Price drop alert message (for snackbar display)
    private val _priceAlertMessage = MutableStateFlow<String?>(null)
    val priceAlertMessage: StateFlow<String?> = _priceAlertMessage.asStateFlow()

    init {
        // 初始化时记录日志 / Log on initialization
        Log.d("WishlistViewModel", "========== WishlistViewModel 初始化 ==========")
        Log.d("WishlistViewModel", "Repository: $repo")
        Log.d("WishlistViewModel", "Wishlist Flow 已创建")
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

    fun updateWishlistItem(
        itemId: String,
        title: String,
        category: String,
        minPrice: Double,
        maxPrice: Double,
        targetPrice: Double = 0.0,
        enablePriceAlert: Boolean = false,
        description: String = ""
    ) {
        _state.value = WishlistUiState.Loading
        viewModelScope.launch {
            try {
                // 先获取现有项以保留原始数据
                val existingItems = wishlist.value
                val existingItem = existingItems.firstOrNull { it.id == itemId }
                    ?: throw IllegalStateException("愿望清单项不存在")
                
                val updatedItem = existingItem.copy(
                    title = title,
                    category = category,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    targetPrice = targetPrice,
                    enablePriceAlert = enablePriceAlert,
                    description = description
                )
                repo.updateWishlistItem(updatedItem)
                Log.d("WishlistViewModel", "愿望清单项更新成功: $itemId, title: ${updatedItem.title}")
                _state.value = WishlistUiState.Success
            } catch (e: Exception) {
                Log.e("WishlistViewModel", "更新愿望清单项失败", e)
                _state.value = WishlistUiState.Error("更新失败: ${e.message ?: "未知错误"}")
            }
        }
    }

    /**
     * 检查价格变化并发送提醒 / Check price changes and send alerts
     * @param messageFormat 消息格式，包含3个%s占位符 / Message format with 3 %s placeholders
     */
    fun checkPriceAlerts(messageFormat: String = "🎉「%s」降价啦！现价 ¥%s，低于目标价 ¥%s") {
        viewModelScope.launch {
            try {
                val alertMessages = repo.checkPriceAlertsWithResult(messageFormat)
                if (alertMessages.isNotEmpty()) {
                    // 合并所有降价提醒消息 / Combine all price drop alert messages
                    val message = alertMessages.joinToString("\n")
                    _priceAlertMessage.value = message
                    Log.d("WishlistViewModel", "发现 ${alertMessages.size} 个降价提醒: $message")
                }
            } catch (e: Exception) {
                Log.e("WishlistViewModel", "检查价格提醒失败", e)
            }
        }
    }
    
    /**
     * 清除降价提醒消息 / Clear price alert message
     */
    fun clearPriceAlertMessage() {
        _priceAlertMessage.value = null
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


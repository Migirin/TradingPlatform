package com.example.tradingplatform.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradingplatform.data.achievement.AchievementRepository
import com.example.tradingplatform.data.achievement.UserAchievement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AchievementUiState {
    data object Idle : AchievementUiState
    data object Loading : AchievementUiState
    data class Error(val message: String) : AchievementUiState
    data object Success : AchievementUiState
}

class AchievementViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repo = AchievementRepository(application)
    
    private val _state = MutableStateFlow<AchievementUiState>(AchievementUiState.Idle)
    val state: StateFlow<AchievementUiState> = _state

    val achievements: StateFlow<List<UserAchievement>> = repo.getUserAchievementsFlow()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        checkAchievements()
    }

    /**
     * 检查并授予成就
     */
    fun checkAchievements() {
        viewModelScope.launch {
            try {
                repo.checkAndGrantAchievements()
            } catch (e: Exception) {
                Log.e("AchievementViewModel", "检查成就失败", e)
            }
        }
    }

    /**
     * 获取已解锁的成就数量
     */
    fun getUnlockedCount(): Int {
        return achievements.value.count { it.isUnlocked }
    }

    /**
     * 获取总成就数量
     */
    fun getTotalCount(): Int {
        return com.example.tradingplatform.data.achievement.AchievementType.values().size
    }
}



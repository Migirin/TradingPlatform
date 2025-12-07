package com.example.tradingplatform.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradingplatform.data.achievement.AchievementRepository
import com.example.tradingplatform.data.chat.ChatMessage
import com.example.tradingplatform.data.chat.ChatRepository
import com.example.tradingplatform.data.chat.ChatConversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed interface ChatUiState {
    data object Idle : ChatUiState
    data object Loading : ChatUiState
    data class Error(val message: String) : ChatUiState
    data object Sending : ChatUiState
    data object SendSuccess : ChatUiState
}

class ChatViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repo = ChatRepository(application)
    private val achievementRepo = AchievementRepository(application)
    private val _state = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val state: StateFlow<ChatUiState> = _state

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    
    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations

    init {
        loadConversations()
    }
    
    fun loadConversations() {
        _state.value = ChatUiState.Loading
        repo.getConversationsFlow()
            .onEach { conversations ->
                _conversations.value = conversations
                _state.value = ChatUiState.Idle
            }
            .launchIn(viewModelScope)
    }

    fun loadMessages() {
        _state.value = ChatUiState.Loading
        repo.getMessagesFlow()
            .onEach { messages ->
                _messages.value = messages
                _state.value = ChatUiState.Idle
            }
            .launchIn(viewModelScope)
    }
    
    fun loadMessagesWithUser(otherUserUid: String) {
        _state.value = ChatUiState.Loading
        repo.getChatWithUserFlow(otherUserUid)
            .onEach { messages ->
                _messages.value = messages
                _state.value = ChatUiState.Idle
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage(
        receiverUid: String,
        receiverEmail: String,
        content: String,
        itemId: String = "",
        itemTitle: String = ""
    ) {
        if (content.isBlank()) return

        _state.value = ChatUiState.Sending
        viewModelScope.launch {
            try {
                repo.sendMessage(receiverUid, receiverEmail, content, itemId, itemTitle)
                _state.value = ChatUiState.SendSuccess
                // 检查成就
                achievementRepo.checkAndGrantAchievements()
            } catch (e: Exception) {
                _state.value = ChatUiState.Error(e.message ?: "发送消息失败")
            }
        }
    }
}



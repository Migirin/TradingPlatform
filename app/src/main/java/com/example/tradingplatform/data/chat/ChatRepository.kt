package com.example.tradingplatform.data.chat

import android.content.Context
import android.util.Log
import com.example.tradingplatform.data.auth.AuthRepository
import com.example.tradingplatform.data.local.AppDatabase
import com.example.tradingplatform.data.local.ChatMessageDao
import com.example.tradingplatform.data.local.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class ChatMessage(
    val id: String = "",
    val senderUid: String = "",
    val senderEmail: String = "",
    val receiverUid: String = "",
    val receiverEmail: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val itemId: String = "",
    val itemTitle: String = ""
)

class ChatRepository(
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "ChatRepository"
    }

    private val database: AppDatabase? = context?.let { AppDatabase.getDatabase(it) }
    private val chatMessageDao: ChatMessageDao? = database?.chatMessageDao()
    private val authRepo: AuthRepository? = context?.let { AuthRepository(it) }

    /**
     * 发送消息
     */
    suspend fun sendMessage(
        receiverUid: String,
        receiverEmail: String,
        content: String,
        itemId: String = "",
        itemTitle: String = ""
    ) = withContext(Dispatchers.IO) {
        if (chatMessageDao == null) {
            throw IllegalStateException("数据库未初始化")
        }

        // 获取当前用户信息
        val currentEmail = authRepo?.getCurrentUserEmail() ?: "dev@example.com"
        val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user_${System.currentTimeMillis()}"

        val message = ChatMessage(
            id = "msg_${System.currentTimeMillis()}_${currentUid}",
            senderUid = currentUid,
            senderEmail = currentEmail,
            receiverUid = receiverUid,
            receiverEmail = receiverEmail,
            content = content,
            timestamp = System.currentTimeMillis(),
            itemId = itemId,
            itemTitle = itemTitle
        )

        val messageEntity = ChatMessageEntity.fromChatMessage(message)
        chatMessageDao.insertMessage(messageEntity)
        Log.d(TAG, "消息发送成功: ${message.id}")
    }

    /**
     * 获取当前用户的所有消息（实时监听）
     */
    fun getMessagesFlow(): Flow<List<ChatMessage>> {
        if (chatMessageDao == null) {
            return flowOf(emptyList())
        }

        return flow {
            val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
            emitAll(
                chatMessageDao.getMessagesForUser(currentUid)
                    .map { entities -> entities.map { it.toChatMessage() } }
            )
        }
    }

    /**
     * 获取与特定用户的聊天消息
     */
    fun getChatWithUserFlow(otherUserUid: String): Flow<List<ChatMessage>> {
        if (chatMessageDao == null) {
            return flowOf(emptyList())
        }

        return flow {
            val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
            emitAll(
                chatMessageDao.getChatWithUser(currentUid, otherUserUid)
                    .map { entities -> entities.map { it.toChatMessage() } }
            )
        }
    }
    
    /**
     * 获取所有对话列表（按最后消息时间排序）
     */
    fun getConversationsFlow(): Flow<List<ChatConversation>> {
        if (chatMessageDao == null) {
            return flowOf(emptyList())
        }

        return flow {
            val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
            val currentEmail = authRepo?.getCurrentUserEmail() ?: ""
            
            emitAll(
                chatMessageDao.getConversations(currentUid)
                    .map { entities ->
                        entities.map { entity ->
                            val message = entity.toChatMessage()
                            // 确定对方用户信息
                            val otherUid = if (message.senderUid == currentUid) {
                                message.receiverUid
                            } else {
                                message.senderUid
                            }
                            val otherEmail = if (message.senderUid == currentUid) {
                                message.receiverEmail
                            } else {
                                message.senderEmail
                            }
                            
                            ChatConversation(
                                otherUserUid = otherUid,
                                otherUserEmail = otherEmail,
                                lastMessage = message.content,
                                lastMessageTime = message.timestamp,
                                itemId = message.itemId,
                                itemTitle = message.itemTitle
                            )
                        }
                    }
            )
        }
    }
}

/**
 * 对话列表项
 */
data class ChatConversation(
    val otherUserUid: String,
    val otherUserEmail: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val itemId: String = "",
    val itemTitle: String = ""
)

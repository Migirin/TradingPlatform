package com.example.tradingplatform.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 聊天消息实体类（Room）
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val senderUid: String,
    val senderEmail: String,
    val receiverUid: String,
    val receiverEmail: String,
    val content: String,
    val timestamp: Long,
    val itemId: String,
    val itemTitle: String
) {
    // 转换为业务层 ChatMessage
    fun toChatMessage(): com.example.tradingplatform.data.chat.ChatMessage {
        return com.example.tradingplatform.data.chat.ChatMessage(
            id = id,
            senderUid = senderUid,
            senderEmail = senderEmail,
            receiverUid = receiverUid,
            receiverEmail = receiverEmail,
            content = content,
            timestamp = timestamp,
            itemId = itemId,
            itemTitle = itemTitle
        )
    }

    companion object {
        fun fromChatMessage(message: com.example.tradingplatform.data.chat.ChatMessage): ChatMessageEntity {
            return ChatMessageEntity(
                id = message.id.ifEmpty { "msg_${System.currentTimeMillis()}_${message.senderUid}" },
                senderUid = message.senderUid,
                senderEmail = message.senderEmail,
                receiverUid = message.receiverUid,
                receiverEmail = message.receiverEmail,
                content = message.content,
                timestamp = message.timestamp,
                itemId = message.itemId,
                itemTitle = message.itemTitle
            )
        }
    }
}









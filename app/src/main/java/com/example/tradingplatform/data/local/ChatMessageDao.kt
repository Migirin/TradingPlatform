package com.example.tradingplatform.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 聊天消息数据访问对象
 */
@Dao
interface ChatMessageDao {
    /**
     * 获取所有消息（按时间倒序）
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    /**
     * 获取当前用户的所有消息
     */
    @Query("SELECT * FROM chat_messages WHERE senderUid = :uid OR receiverUid = :uid ORDER BY timestamp DESC")
    fun getMessagesForUser(uid: String): Flow<List<ChatMessageEntity>>

    /**
     * 获取与特定用户的聊天消息
     */
    @Query("SELECT * FROM chat_messages WHERE (senderUid = :currentUid AND receiverUid = :otherUid) OR (senderUid = :otherUid AND receiverUid = :currentUid) ORDER BY timestamp ASC")
    fun getChatWithUser(currentUid: String, otherUid: String): Flow<List<ChatMessageEntity>>

    /**
     * 插入消息
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    /**
     * 删除所有消息
     */
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    /**
     * 获取用户的消息数量
     */
    @Query("SELECT COUNT(*) FROM chat_messages WHERE senderUid = :uid OR receiverUid = :uid")
    suspend fun getMessageCountForUser(uid: String): Int
    
    /**
     * 获取所有对话用户（去重，返回最后一条消息）
     * 返回与当前用户有对话的所有其他用户，以及最后一条消息
     */
    @Query("""
        SELECT * FROM chat_messages 
        WHERE id IN (
            SELECT MAX(id) 
            FROM chat_messages 
            WHERE senderUid = :currentUid OR receiverUid = :currentUid
            GROUP BY 
                CASE 
                    WHEN senderUid = :currentUid THEN receiverUid 
                    ELSE senderUid 
                END
        )
        ORDER BY timestamp DESC
    """)
    fun getConversations(currentUid: String): Flow<List<ChatMessageEntity>>
}





package com.example.tradingplatform.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 用户数据访问对象
 */
@Dao
interface UserDao {
    /**
     * 根据邮箱查询用户
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    /**
     * 插入新用户（如果已存在则替换）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    /**
     * 更新用户信息
     */
    @Update
    suspend fun updateUser(user: User)

    /**
     * 根据验证码查询用户
     */
    @Query("SELECT * FROM users WHERE verificationCode = :code AND verificationCodeExpiry > :currentTime LIMIT 1")
    suspend fun getUserByVerificationCode(code: String, currentTime: Long = System.currentTimeMillis()): User?

    /**
     * 清除验证码
     */
    @Query("UPDATE users SET verificationCode = NULL, verificationCodeExpiry = NULL WHERE email = :email")
    suspend fun clearVerificationCode(email: String)

    /**
     * 删除用户
     */
    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUser(email: String)

    /**
     * 获取所有用户（用于调试）
     */
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
}


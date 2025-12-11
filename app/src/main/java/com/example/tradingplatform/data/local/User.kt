package com.example.tradingplatform.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户实体类
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val email: String, // 使用邮箱作为主键
    val passwordHash: String, // 密码的哈希值
    val uid: String,
    val emailVerified: Boolean = false, // 邮箱验证状态
    val verificationCode: String? = null, // 验证码
    val verificationCodeExpiry: Long? = null, // 验证码过期时间
    val displayName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


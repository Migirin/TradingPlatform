package com.example.tradingplatform.data.auth

import android.content.Context
import android.util.Log
import com.example.tradingplatform.data.local.AppDatabase
import com.example.tradingplatform.data.local.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * 测试用户种子数据：自动创建两个测试账号供开发测试使用 / Test user seed data: automatically create two test accounts for development and testing
 */
object TestUserSeeder {

    private const val TAG = "TestUserSeeder"

    /**
     * 密码哈希函数（与 AuthRepository 保持一致）/ Password hashing function (consistent with AuthRepository)
     */
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * 确保测试用户存在于数据库中 / Ensure test users exist in database
     * 如果用户不存在，则创建；如果已存在，则跳过 / If user doesn't exist, create it; if exists, skip
     */
    suspend fun ensureTestUsers(context: Context) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val userDao = db.userDao()

            val now = System.currentTimeMillis()

            // 测试账号1 / Test account 1
            val testUser1Email = "testuser1@ucdconnect.ie"
            val testUser1Password = "test123"
            val testUser1 = userDao.getUserByEmail(testUser1Email)
            
            if (testUser1 == null) {
                val user1 = User(
                    email = testUser1Email,
                    passwordHash = hashPassword(testUser1Password),
                    uid = "test_user_1_${now}",
                    emailVerified = true, // 测试账号直接设置为已验证 / Test account directly set as verified
                    verificationCode = null,
                    verificationCodeExpiry = null,
                    displayName = "testuser1",
                    createdAt = now,
                    updatedAt = now
                )
                userDao.insertUser(user1)
                Log.d(TAG, "已创建测试账号1: $testUser1Email / $testUser1Password")
            } else {
                Log.d(TAG, "测试账号1已存在: $testUser1Email")
            }

            // 测试账号2 / Test account 2
            val testUser2Email = "testuser2@ucdconnect.ie"
            val testUser2Password = "test456"
            val testUser2 = userDao.getUserByEmail(testUser2Email)
            
            if (testUser2 == null) {
                val user2 = User(
                    email = testUser2Email,
                    passwordHash = hashPassword(testUser2Password),
                    uid = "test_user_2_${now}",
                    emailVerified = true, // 测试账号直接设置为已验证 / Test account directly set as verified
                    verificationCode = null,
                    verificationCodeExpiry = null,
                    displayName = "testuser2",
                    createdAt = now,
                    updatedAt = now
                )
                userDao.insertUser(user2)
                Log.d(TAG, "已创建测试账号2: $testUser2Email / $testUser2Password")
            } else {
                Log.d(TAG, "测试账号2已存在: $testUser2Email")
            }

            Log.d(TAG, "测试用户初始化完成")
        }
    }
}


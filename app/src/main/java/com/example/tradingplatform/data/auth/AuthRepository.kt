package com.example.tradingplatform.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.tradingplatform.data.local.AppDatabase
import com.example.tradingplatform.data.local.User
import com.example.tradingplatform.data.local.UserDao
import com.example.tradingplatform.data.supabase.SupabaseApi
import com.example.tradingplatform.data.supabase.SupabaseClient
import com.example.tradingplatform.data.supabase.SupabaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * 本地存储的认证系统（使用 Room 数据库）
 */
class AuthRepository(
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "AuthRepository"
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_CURRENT_USER_EMAIL = "current_user_email"
        private const val KEY_CURRENT_USER_UID = "current_user_uid"
        
        // 保存的登录凭据
        private const val KEY_SAVED_EMAIL = "saved_email"
        private const val KEY_SAVED_PASSWORD = "saved_password"
        
        // 临时存储未验证用户信息的键
        private const val KEY_PENDING_EMAIL = "pending_email"
        private const val KEY_PENDING_PASSWORD_HASH = "pending_password_hash"
        private const val KEY_PENDING_VERIFICATION_CODE = "pending_verification_code"
        private const val KEY_PENDING_VERIFICATION_CODE_EXPIRY = "pending_verification_code_expiry"
        private const val KEY_PENDING_UID = "pending_uid"
        private const val KEY_PENDING_DISPLAY_NAME = "pending_display_name"
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Room 数据库
    private val database: AppDatabase? = context?.let { AppDatabase.getDatabase(it) }
    private val userDao: UserDao? = database?.userDao()
    
    // Supabase API
    private val supabaseApi: SupabaseApi? = context?.let { SupabaseClient.getApi() }

    /**
     * 简单的密码哈希（实际应该使用更安全的方法，如 bcrypt）
     */
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * 注册新用户（发送验证邮件）
     * 注意：只有通过验证码验证后，用户才会被存储到数据库
     */
    suspend fun register(email: String, password: String): Result<Unit> = runCatching {
        Log.d(TAG, "开始注册: $email")
        
        if (userDao == null) {
            throw IllegalStateException("数据库未初始化")
        }
        
        withContext(Dispatchers.IO) {
            val emailLower = email.lowercase()
            
            // 验证邮箱格式
            if (!emailLower.endsWith("@ucdconnect.ie", ignoreCase = true)) {
                throw IllegalArgumentException("请使用 @ucdconnect.ie 校内邮箱")
            }
            
            // 验证密码长度
            if (password.length < 6) {
                throw IllegalArgumentException("密码至少需要6个字符")
            }
            
            // 检查本地数据库是否已存在
            val existingLocalUser = userDao.getUserByEmail(emailLower)
            if (existingLocalUser != null) {
                Log.d(TAG, "本地数据库已存在该邮箱: $emailLower")
                throw IllegalArgumentException("已注册")
            }
            
            // 检查 Supabase 是否已存在
            try {
                val supabaseResponse = supabaseApi?.getUserByEmail("eq.$emailLower")
                if (supabaseResponse?.isSuccessful == true) {
                    val supabaseUsers = supabaseResponse.body() ?: emptyList()
                    if (supabaseUsers.isNotEmpty()) {
                        Log.d(TAG, "Supabase 已存在该邮箱: $emailLower")
                        throw IllegalArgumentException("已注册")
                    }
                }
            } catch (e: IllegalArgumentException) {
                // 如果是我们抛出的"已注册"异常，继续抛出
                throw e
            } catch (e: Exception) {
                // 其他异常（如网络错误）只记录日志，不阻止注册
                Log.w(TAG, "检查 Supabase 用户时出错，继续注册流程", e)
            }
            
            // 生成验证码
            val verificationCode = EmailVerificationService.generateVerificationCode()
            val codeExpiry = EmailVerificationService.getCodeExpiryTime()
            
            // 计算密码哈希和生成 UID
            val passwordHash = hashPassword(password)
            val uid = "local_${System.currentTimeMillis()}_${email.hashCode()}"
            val displayName = email.substringBefore('@')
            
            // 将用户信息临时存储到 SharedPreferences（不存储到数据库）
            prefs?.edit()?.apply {
                putString(KEY_PENDING_EMAIL, emailLower)
                putString(KEY_PENDING_PASSWORD_HASH, passwordHash)
                putString(KEY_PENDING_VERIFICATION_CODE, verificationCode)
                putLong(KEY_PENDING_VERIFICATION_CODE_EXPIRY, codeExpiry)
                putString(KEY_PENDING_UID, uid)
                putString(KEY_PENDING_DISPLAY_NAME, displayName)
                apply()
            }
            
            Log.d(TAG, "用户信息已临时存储，等待验证: $emailLower")
            
            // 发送验证邮件（真实发送）
            EmailVerificationService.sendVerificationEmail(emailLower, verificationCode).getOrThrow()
            
            Log.d(TAG, "注册成功，验证邮件已发送: $email")
        }
    }

    /**
     * 验证邮箱（使用验证码）
     * 验证成功后，才将用户存储到数据库和 Supabase
     */
    suspend fun verifyEmail(email: String, code: String): Result<Unit> = runCatching {
        Log.d(TAG, "验证邮箱: $email, 验证码: $code")
        
        if (userDao == null) {
            throw IllegalStateException("数据库未初始化")
        }
        
        withContext(Dispatchers.IO) {
            val emailLower = email.lowercase()
            
            // 从 SharedPreferences 读取临时用户信息
            val pendingEmail = prefs?.getString(KEY_PENDING_EMAIL, null)
            val pendingPasswordHash = prefs?.getString(KEY_PENDING_PASSWORD_HASH, null)
            val pendingVerificationCode = prefs?.getString(KEY_PENDING_VERIFICATION_CODE, null)
            val pendingVerificationCodeExpiry = prefs?.getLong(KEY_PENDING_VERIFICATION_CODE_EXPIRY, 0L)
            val pendingUid = prefs?.getString(KEY_PENDING_UID, null)
            val pendingDisplayName = prefs?.getString(KEY_PENDING_DISPLAY_NAME, null)
            
            // 检查是否有待验证的用户信息
            if (pendingEmail == null || pendingEmail != emailLower) {
                throw IllegalArgumentException("请先注册")
            }
            
            // 检查验证码
            if (pendingVerificationCode != code) {
                throw IllegalArgumentException("验证码错误")
            }
            
            // 检查验证码是否过期
            if (!EmailVerificationService.isCodeValid(pendingVerificationCodeExpiry)) {
                // 清除临时信息
                clearPendingUserInfo()
                throw IllegalArgumentException("验证码已过期，请重新注册")
            }
            
            // 再次检查用户是否已存在（防止并发注册）
            val existingUser = userDao.getUserByEmail(emailLower)
            if (existingUser != null) {
                // 用户已存在，清除临时信息
                clearPendingUserInfo()
                throw IllegalArgumentException("该邮箱已注册，请直接登录")
            }
            
            // 验证成功，创建用户并存储到数据库
            val newUser = User(
                email = emailLower,
                passwordHash = pendingPasswordHash ?: throw IllegalStateException("密码哈希丢失"),
                uid = pendingUid ?: throw IllegalStateException("UID 丢失"),
                emailVerified = true, // 已验证
                verificationCode = null,
                verificationCodeExpiry = null,
                displayName = pendingDisplayName ?: emailLower.substringBefore('@'),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // 存储到本地数据库
            userDao.insertUser(newUser)
            Log.d(TAG, "用户已存储到本地数据库: $emailLower")
            
            // 同步到 Supabase
            try {
                val supabaseUserData = SupabaseUser.fromLocalUser(newUser)
                val response = supabaseApi?.createUser(supabaseUserData)
                if (response?.isSuccessful == true) {
                    Log.d(TAG, "用户已同步到 Supabase: $emailLower")
                } else {
                    val errorBody = response?.errorBody()?.string()
                    Log.w(TAG, "Supabase 同步失败: HTTP ${response?.code()} - $errorBody")
                    // 即使 Supabase 失败，本地数据仍然保存
                }
            } catch (e: Exception) {
                Log.w(TAG, "Supabase 同步失败（用户仍在本地）", e)
                // 即使 Supabase 失败，本地数据仍然保存
            }
            
            // 清除临时信息
            clearPendingUserInfo()
            
            Log.d(TAG, "邮箱验证成功，用户已存储: $emailLower")
        }
    }
    
    /**
     * 清除临时存储的待验证用户信息
     */
    private fun clearPendingUserInfo() {
        prefs?.edit()?.apply {
            remove(KEY_PENDING_EMAIL)
            remove(KEY_PENDING_PASSWORD_HASH)
            remove(KEY_PENDING_VERIFICATION_CODE)
            remove(KEY_PENDING_VERIFICATION_CODE_EXPIRY)
            remove(KEY_PENDING_UID)
            remove(KEY_PENDING_DISPLAY_NAME)
            apply()
        }
    }

    /**
     * 登录
     * 优先从本地数据库验证，如果本地没有则尝试从 Supabase 同步
     */
    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        Log.d(TAG, "开始登录: $email")
        
        if (userDao == null) {
            throw IllegalStateException("数据库未初始化")
        }
        
        withContext(Dispatchers.IO) {
            val emailLower = email.lowercase()
            
            // 验证邮箱格式
            if (!emailLower.endsWith("@ucdconnect.ie", ignoreCase = true)) {
                throw IllegalArgumentException("请使用 @ucdconnect.ie 校内邮箱")
            }
            
            val passwordHash = hashPassword(password)
            
            // 先尝试从本地数据库获取用户
            var user = userDao.getUserByEmail(emailLower)
            
            // 如果本地没有用户，尝试从 Supabase 获取
            if (user == null) {
                Log.d(TAG, "本地没有用户，尝试从 Supabase 获取: $email")
                user = trySyncUserFromSupabase(emailLower, passwordHash)
            }
            
            // 如果仍然没有用户，说明用户不存在
            if (user == null) {
                throw IllegalArgumentException("用户不存在，请先注册")
            }
            
            // 验证密码
            if (user.passwordHash != passwordHash) {
                Log.e(TAG, "密码错误")
                throw IllegalArgumentException("密码错误")
            }
            
            // 检查邮箱是否已验证
            if (!user.emailVerified) {
                throw IllegalArgumentException("邮箱未验证，请先验证邮箱")
            }
            
            Log.d(TAG, "登录成功: $email")
            
            // 更新最后登录时间
            val updatedUser = user.copy(updatedAt = System.currentTimeMillis())
            userDao.updateUser(updatedUser)
            
            // 保存当前登录用户到 SharedPreferences（用于快速访问）
            prefs?.edit()?.apply {
                putString(KEY_CURRENT_USER_EMAIL, emailLower)
                putString(KEY_CURRENT_USER_UID, user.uid)
                apply()
            }
            
            // 保存邮箱和密码（用于下次自动填充）
            saveCredentials(emailLower, password)
        }
    }
    
    /**
     * 从 Supabase 同步用户到本地数据库
     * 注意：Supabase 不存储密码哈希，所以需要用户输入密码来创建本地用户记录
     */
    private suspend fun trySyncUserFromSupabase(email: String, passwordHash: String): User? {
        return try {
            val response = supabaseApi?.getUserByEmail("eq.$email")
            if (response?.isSuccessful == true) {
                val supabaseUsers = response.body() ?: emptyList()
                if (supabaseUsers.isNotEmpty()) {
                    val supabaseUser = supabaseUsers.first()
                    Log.d(TAG, "从 Supabase 找到用户: $email")
                    
                    // 创建本地用户记录（使用提供的密码哈希）
                    // 注意：由于 Supabase 不存储密码，我们使用用户输入的密码哈希
                    val localUser = User(
                        email = supabaseUser.email,
                        passwordHash = passwordHash, // 使用用户输入的密码哈希
                        uid = supabaseUser.uid,
                        emailVerified = supabaseUser.emailVerified,
                        verificationCode = null,
                        verificationCodeExpiry = null,
                        displayName = supabaseUser.displayName ?: email.substringBefore('@'),
                        createdAt = System.currentTimeMillis(), // 使用当前时间，因为 Supabase 的时间格式需要解析
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    // 保存到本地数据库
                    userDao?.insertUser(localUser)
                    Log.d(TAG, "用户已从 Supabase 同步到本地: $email")
                    
                    return localUser
                }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "从 Supabase 同步用户失败", e)
            null
        }
    }

    /**
     * 重新发送验证邮件
     */
    suspend fun resendVerificationEmail(email: String): Result<Unit> = runCatching {
        Log.d(TAG, "重新发送验证邮件: $email")
        
        if (userDao == null) {
            throw IllegalStateException("数据库未初始化")
        }
        
        withContext(Dispatchers.IO) {
            val emailLower = email.lowercase()
            
            // 检查是否有待验证的用户信息（从 SharedPreferences）
            val pendingEmail = prefs?.getString(KEY_PENDING_EMAIL, null)
            if (pendingEmail != emailLower) {
                // 如果没有待验证信息，检查数据库中是否有未验证的用户
                val user = userDao.getUserByEmail(emailLower)
                if (user == null) {
                    throw IllegalArgumentException("用户不存在，请先注册")
                }
                if (user.emailVerified) {
                    throw IllegalArgumentException("邮箱已验证，无需重新发送")
                }
                
                // 生成新的验证码并更新数据库
                val verificationCode = EmailVerificationService.generateVerificationCode()
                val codeExpiry = EmailVerificationService.getCodeExpiryTime()
                
                val updatedUser = user.copy(
                    verificationCode = verificationCode,
                    verificationCodeExpiry = codeExpiry,
                    updatedAt = System.currentTimeMillis()
                )
                userDao.updateUser(updatedUser)
                
                // 发送验证邮件
                EmailVerificationService.sendVerificationEmail(emailLower, verificationCode).getOrThrow()
            } else {
                // 有待验证信息，生成新的验证码并更新 SharedPreferences
                val verificationCode = EmailVerificationService.generateVerificationCode()
                val codeExpiry = EmailVerificationService.getCodeExpiryTime()
                
                prefs?.edit()?.apply {
                    putString(KEY_PENDING_VERIFICATION_CODE, verificationCode)
                    putLong(KEY_PENDING_VERIFICATION_CODE_EXPIRY, codeExpiry)
                    apply()
                }
                
                // 发送验证邮件
                EmailVerificationService.sendVerificationEmail(emailLower, verificationCode).getOrThrow()
            }
            
            Log.d(TAG, "验证邮件已重新发送: $email")
        }
    }

    suspend fun isEmailVerified(): Boolean {
        val currentEmail = prefs?.getString(KEY_CURRENT_USER_EMAIL, null) ?: return false
        return withContext(Dispatchers.IO) {
            val user = userDao?.getUserByEmail(currentEmail.lowercase())
            user?.emailVerified ?: true // 本地模式默认已验证
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val currentEmail = prefs?.getString(KEY_CURRENT_USER_EMAIL, null) ?: return false
        return withContext(Dispatchers.IO) {
            val user = userDao?.getUserByEmail(currentEmail.lowercase())
            user != null
        }
    }
    
    fun getCurrentUserEmail(): String? {
        return prefs?.getString(KEY_CURRENT_USER_EMAIL, null)
    }
    
    suspend fun getCurrentUserUid(): String? {
        val currentEmail = getCurrentUserEmail() ?: return null
        return withContext(Dispatchers.IO) {
            val user = userDao?.getUserByEmail(currentEmail.lowercase())
            user?.uid
        }
    }
    
    /**
     * 删除用户（用于测试/清理数据）
     */
    suspend fun deleteUser(email: String): Result<Unit> = runCatching {
        Log.d(TAG, "删除用户: $email")
        
        if (userDao == null) {
            throw IllegalStateException("数据库未初始化")
        }
        
        withContext(Dispatchers.IO) {
            val emailLower = email.lowercase()
            val user = userDao.getUserByEmail(emailLower)
            
            if (user == null) {
                throw IllegalArgumentException("用户不存在")
            }
            
            // 删除数据库中的用户
            userDao.deleteUser(emailLower)
            
            // 如果删除的是当前登录用户，清除登录状态
            val currentEmail = prefs?.getString(KEY_CURRENT_USER_EMAIL, null)
            if (currentEmail?.lowercase() == emailLower) {
                prefs?.edit()?.apply {
                    remove(KEY_CURRENT_USER_EMAIL)
                    remove(KEY_CURRENT_USER_UID)
                    apply()
                }
            }
            
            Log.d(TAG, "用户已删除: $email")
        }
    }
    
    fun logout() {
        prefs?.edit()?.apply {
            remove(KEY_CURRENT_USER_EMAIL)
            remove(KEY_CURRENT_USER_UID)
            // 注意：登出时不删除保存的凭据，以便下次登录时自动填充
            apply()
        }
        Log.d(TAG, "用户已登出")
    }
    
    /**
     * 保存登录凭据（邮箱和密码）
     */
    fun saveCredentials(email: String, password: String) {
        prefs?.edit()?.apply {
            putString(KEY_SAVED_EMAIL, email.lowercase())
            putString(KEY_SAVED_PASSWORD, password)
            apply()
        }
        Log.d(TAG, "已保存登录凭据")
    }
    
    /**
     * 获取保存的邮箱
     */
    fun getSavedEmail(): String? {
        return prefs?.getString(KEY_SAVED_EMAIL, null)
    }
    
    /**
     * 获取保存的密码
     */
    fun getSavedPassword(): String? {
        return prefs?.getString(KEY_SAVED_PASSWORD, null)
    }
    
    /**
     * 清除保存的登录凭据
     */
    fun clearSavedCredentials() {
        prefs?.edit()?.apply {
            remove(KEY_SAVED_EMAIL)
            remove(KEY_SAVED_PASSWORD)
            apply()
        }
        Log.d(TAG, "已清除保存的登录凭据")
    }
}

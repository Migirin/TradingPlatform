package com.example.tradingplatform.data.auth

import android.util.Log
import com.example.tradingplatform.data.auth.email.EmailService
import java.util.Random

/**
 * 邮件验证服务
 * 使用真实的邮件服务 API 发送验证邮件
 */
object EmailVerificationService {
    private const val TAG = "EmailVerificationService"
    private const val CODE_LENGTH = 6
    private const val CODE_EXPIRY_MINUTES = 30L // 验证码30分钟有效

    /**
     * 生成验证码
     */
    fun generateVerificationCode(): String {
        val random = Random()
        val code = StringBuilder()
        repeat(CODE_LENGTH) {
            code.append(random.nextInt(10))
        }
        return code.toString()
    }

    /**
     * 计算验证码过期时间
     */
    fun getCodeExpiryTime(): Long {
        return System.currentTimeMillis() + (CODE_EXPIRY_MINUTES * 60 * 1000)
    }

    /**
     * 发送验证邮件（真实发送）
     * 使用 SendGrid API 发送邮件
     */
    suspend fun sendVerificationEmail(email: String, code: String): Result<Unit> = runCatching {
        Log.d(TAG, "发送验证邮件到: $email")
        Log.d(TAG, "验证码: $code (有效期: ${CODE_EXPIRY_MINUTES}分钟)")
        
        // 调用真实的邮件服务 API
        EmailService.sendVerificationEmail(email, code).getOrThrow()
        
        Log.d(TAG, "验证邮件已发送: $email")
    }

    /**
     * 验证验证码是否有效
     */
    fun isCodeValid(codeExpiry: Long?): Boolean {
        if (codeExpiry == null) return false
        return System.currentTimeMillis() < codeExpiry
    }
}


package com.example.tradingplatform.data.auth.email

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * 邮件服务接口
 * 使用 SendGrid API 发送邮件
 * 
 * 配置说明：
 * 1. 注册 SendGrid 账号：https://sendgrid.com/
 * 2. 创建 API Key：Settings -> API Keys -> Create API Key
 * 3. 将 API Key 配置到 EmailServiceConfig.API_KEY
 */
object EmailService {
    private const val TAG = "EmailService"
    private const val SENDGRID_API_URL = "https://api.sendgrid.com/v3/mail/send"
    
    // TODO: 将 API Key 配置到安全位置（如 BuildConfig 或环境变量）
    // 注意：不要将 API Key 提交到版本控制系统
    private const val API_KEY = "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" // 请替换为您的 SendGrid API Key
    
    // 发件人邮箱（需要在 SendGrid 中验证）
    private const val FROM_EMAIL = "xinghang.cao@ucdconnect.ie" // 请替换为您的验证邮箱
    private const val FROM_NAME = "BJUT SecondHand"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    
    private val gson = Gson()

    /**
     * 发送验证邮件
     */
    suspend fun sendVerificationEmail(
        toEmail: String,
        verificationCode: String
    ): Result<Unit> = runCatching {
        if (API_KEY == "YOUR_SENDGRID_API_KEY_HERE") {
            throw IllegalStateException(
                "请先配置 SendGrid API Key。\n" +
                "1. 注册 SendGrid 账号：https://sendgrid.com/\n" +
                "2. 创建 API Key\n" +
                "3. 在 EmailService.kt 中配置 API_KEY"
            )
        }
        
        withContext(Dispatchers.IO) {
            val emailContent = buildEmailContent(verificationCode)
            
            val requestBody = SendGridRequest(
                personalizations = listOf(
                    Personalization(
                        to = listOf(EmailAddress(toEmail))
                    )
                ),
                from = EmailAddress(FROM_EMAIL, FROM_NAME),
                subject = "BJUT SecondHand - 邮箱验证",
                content = listOf(
                    EmailContent(
                        type = "text/html",
                        value = emailContent
                    )
                )
            )
            
            val jsonBody = gson.toJson(requestBody)
            val request = Request.Builder()
                .url(SENDGRID_API_URL)
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                Log.d(TAG, "验证邮件发送成功: $toEmail")
            } else {
                val errorBody = response.body?.string() ?: "未知错误"
                Log.e(TAG, "发送邮件失败: HTTP ${response.code}")
                Log.e(TAG, "错误响应: $errorBody")
                Log.e(TAG, "收件人: $toEmail")
                Log.e(TAG, "发件人: $FROM_EMAIL")
                
                // 详细错误信息
                when (response.code) {
                    400 -> throw Exception("请求格式错误，请检查邮箱地址格式")
                    401 -> throw Exception("SendGrid API Key 无效或已过期，请检查配置")
                    403 -> throw Exception("SendGrid API Key 权限不足或被限制")
                    429 -> throw Exception("发送频率过高，请稍后再试（SendGrid 免费版每天限制 100 封）")
                    500, 502, 503, 504 -> throw Exception("SendGrid 服务暂时不可用，请稍后再试")
                    else -> throw Exception("发送邮件失败: HTTP ${response.code} - $errorBody")
                }
            }
        }
    }

    /**
     * 构建邮件内容（HTML）
     */
    private fun buildEmailContent(verificationCode: String): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background-color: #f9f9f9; }
                .code { font-size: 24px; font-weight: bold; color: #4CAF50; text-align: center; padding: 20px; background-color: white; border: 2px dashed #4CAF50; margin: 20px 0; }
                .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>BJUT SecondHand</h1>
                </div>
                <div class="content">
                    <h2>邮箱验证</h2>
                    <p>感谢您注册 BJUT SecondHand 二手交易平台！</p>
                    <p>您的验证码是：</p>
                    <div class="code">$verificationCode</div>
                    <p>此验证码有效期为 30 分钟，请尽快完成验证。</p>
                    <p>如果您没有注册此账号，请忽略此邮件。</p>
                </div>
                <div class="footer">
                    <p>© 2024 BJUT SecondHand. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    // SendGrid API 请求数据类
    private data class SendGridRequest(
        @SerializedName("personalizations")
        val personalizations: List<Personalization>,
        @SerializedName("from")
        val from: EmailAddress,
        @SerializedName("subject")
        val subject: String,
        @SerializedName("content")
        val content: List<EmailContent>
    )

    private data class Personalization(
        @SerializedName("to")
        val to: List<EmailAddress>
    )

    private data class EmailAddress(
        @SerializedName("email")
        val email: String,
        @SerializedName("name")
        val name: String? = null
    )

    private data class EmailContent(
        @SerializedName("type")
        val type: String,
        @SerializedName("value")
        val value: String
    )
}


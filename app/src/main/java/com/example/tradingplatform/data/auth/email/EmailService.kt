package com.example.tradingplatform.data.auth.email

import android.util.Log
import com.example.tradingplatform.data.vision.ApiConfig
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
    
    // 从 ApiConfig 读取配置（从 local.properties 读取，与项目其他 API 配置保持一致）
    // 注意：不要将 API Key 提交到版本控制系统
    private val API_KEY = ApiConfig.SendGrid.API_KEY
    
    // 发件人邮箱（需要在 SendGrid 中验证）
    private val FROM_EMAIL = ApiConfig.SendGrid.FROM_EMAIL
    private val FROM_NAME = ApiConfig.SendGrid.FROM_NAME
    
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
        if (!ApiConfig.isSendGridConfigured() || API_KEY.isBlank()) {
            throw IllegalStateException(
                "请先配置 SendGrid API Key。\n" +
                "1. 注册 SendGrid 账号：https://sendgrid.com/\n" +
                "2. 创建 API Key（确保有 'Mail Send' 权限）\n" +
                "3. 在 local.properties 文件中添加：\n" +
                "   sendgrid.api.key=您的API密钥\n" +
                "   sendgrid.from.email=您的验证邮箱"
            )
        }
        
        withContext(Dispatchers.IO) {
            // 调试日志：检查 API Key 是否正确读取
            Log.d(TAG, "SendGrid API Key 长度: ${API_KEY.length}")
            Log.d(TAG, "SendGrid API Key (前10字符): ${API_KEY.take(10)}...")
            Log.d(TAG, "SendGrid API Key (后10字符): ...${API_KEY.takeLast(10)}")
            Log.d(TAG, "发件人邮箱: $FROM_EMAIL")
            Log.d(TAG, "收件人邮箱: $toEmail")
            
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
                Log.e(TAG, "使用的 API Key (前20字符): ${API_KEY.take(20)}...")
                Log.e(TAG, "使用的 API Key (后20字符): ...${API_KEY.takeLast(20)}")
                
                // 详细错误信息
                when (response.code) {
                    400 -> throw Exception("请求格式错误，请检查邮箱地址格式")
                    401 -> throw Exception(
                        "SendGrid API Key 无效或已过期！\n\n" +
                        "解决方法：\n" +
                        "1. 登录 https://sendgrid.com/\n" +
                        "2. 进入 Settings -> API Keys\n" +
                        "3. 创建新的 API Key（确保有 'Mail Send' 权限）\n" +
                        "4. 在 ApiConfig.kt 中更新 SendGrid.API_KEY 的值\n" +
                        "5. 重新构建并运行应用\n\n" +
                        "当前使用的 API Key (前20字符): ${API_KEY.take(20)}..."
                    )
                    403 -> throw Exception("SendGrid API Key 权限不足或被限制，请检查 API Key 权限设置")
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


package com.example.tradingplatform.data.vision

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*
import java.util.TimeZone

/**
 * 第三方商品识别服务
 * 
 * 支持多种第三方 API：
 * - 阿里云商品识别
 * - 腾讯云商品识别
 * - 百度 AI 商品识别
 */
class ThirdPartyRecognitionService(
    private val apiType: ApiType = ApiType.ALIYUN
) {
    companion object {
        private const val TAG = "ThirdPartyRecognition"
        private val client = OkHttpClient()
        private val gson = Gson()
    }

    enum class ApiType {
        ALIYUN,    // 阿里云
        TENCENT,   // 腾讯云
        BAIDU      // 百度 AI
    }

    /**
     * 识别商品（使用第三方 API）
     */
    suspend fun recognizeProduct(bitmap: Bitmap): List<ProductRecognitionResult> = withContext(Dispatchers.IO) {
        try {
            val imageBase64 = bitmapToBase64(bitmap)
            
            when (apiType) {
                ApiType.ALIYUN -> recognizeWithAliyun(imageBase64)
                ApiType.TENCENT -> recognizeWithTencent(imageBase64)
                ApiType.BAIDU -> recognizeWithBaidu(imageBase64)
            }
        } catch (e: Exception) {
            Log.e(TAG, "第三方识别失败", e)
            emptyList()
        }
    }

    /**
     * 阿里云商品识别
     * 使用通用物体识别 API（商品识别需要单独开通，这里使用通用识别）
     */
    private suspend fun recognizeWithAliyun(imageBase64: String): List<ProductRecognitionResult> {
        if (!ApiConfig.isAliyunConfigured()) {
            Log.w(TAG, "阿里云配置不完整")
            return emptyList()
        }

        try {
            // 使用阿里云图像识别 API
            val url = "https://imageseg.cn-shanghai.aliyuncs.com/"
            val action = "RecognizeImageColor"
            
            val params = mutableMapOf<String, String>(
                "Action" to action,
                "Format" to "JSON",
                "Version" to "2019-12-30",
                "AccessKeyId" to ApiConfig.Aliyun.ACCESS_KEY_ID,
                "SignatureMethod" to "HMAC-SHA1",
                "Timestamp" to getUTCTime(),
                "SignatureVersion" to "1.0",
                "SignatureNonce" to UUID.randomUUID().toString(),
                "ImageURL" to "data:image/jpeg;base64,$imageBase64"
            )

            // 生成签名
            val signature = generateAliyunSignature(params, ApiConfig.Aliyun.ACCESS_KEY_SECRET)
            params["Signature"] = signature

            // 构建请求
            val requestBody = params.entries.joinToString("&") { 
                "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" 
            }.toRequestBody("application/x-www-form-urlencoded".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return emptyList()

            Log.d(TAG, "阿里云识别响应: $responseBody")

            // 解析响应（简化版，实际需要根据 API 文档解析）
            return parseAliyunResponse(responseBody)
        } catch (e: Exception) {
            Log.e(TAG, "阿里云识别失败", e)
            return emptyList()
        }
    }

    /**
     * 腾讯云商品识别
     */
    private suspend fun recognizeWithTencent(imageBase64: String): List<ProductRecognitionResult> {
        if (!ApiConfig.isTencentConfigured()) {
            Log.w(TAG, "腾讯云配置不完整")
            return emptyList()
        }

        try {
            // 腾讯云 API 调用（需要实现签名算法）
            // 这里提供简化实现
            Log.d(TAG, "腾讯云识别（需要实现签名算法）")
            return emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "腾讯云识别失败", e)
            return emptyList()
        }
    }

    /**
     * 百度 AI 商品识别
     */
    private suspend fun recognizeWithBaidu(imageBase64: String): List<ProductRecognitionResult> {
        if (!ApiConfig.isBaiduConfigured()) {
            Log.w(TAG, "百度 AI 配置不完整")
            return emptyList()
        }

        try {
            // 1. 获取 Access Token
            val token = getBaiduAccessToken()
            if (token.isEmpty()) {
                return emptyList()
            }

            // 2. 调用商品识别 API
            val url = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general?access_token=$token"
            
            val requestBody = "image=$imageBase64".toRequestBody("application/x-www-form-urlencoded".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return emptyList()

            Log.d(TAG, "百度 AI 识别响应: $responseBody")

            return parseBaiduResponse(responseBody)
        } catch (e: Exception) {
            Log.e(TAG, "百度 AI 识别失败", e)
            return emptyList()
        }
    }

    /**
     * 获取百度 AI Access Token
     */
    private suspend fun getBaiduAccessToken(): String {
        try {
            val url = "https://aip.baidubce.com/oauth/2.0/token"
            val params = "grant_type=client_credentials&client_id=${ApiConfig.Baidu.API_KEY}&client_secret=${ApiConfig.Baidu.SECRET_KEY}"
            
            val request = Request.Builder()
                .url("$url?$params")
                .post("".toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return ""
            
            val json = JSONObject(responseBody)
            return json.optString("access_token", "")
        } catch (e: Exception) {
            Log.e(TAG, "获取百度 AI Token 失败", e)
            return ""
        }
    }

    /**
     * 解析阿里云响应
     */
    private fun parseAliyunResponse(responseBody: String): List<ProductRecognitionResult> {
        // 简化解析，实际需要根据 API 文档
        return emptyList()
    }

    /**
     * 解析百度 AI 响应
     */
    private fun parseBaiduResponse(responseBody: String): List<ProductRecognitionResult> {
        try {
            val json = JSONObject(responseBody)
            val resultArray = json.optJSONArray("result") ?: return emptyList()
            
            val results = mutableListOf<ProductRecognitionResult>()
            for (i in 0 until resultArray.length()) {
                val item = resultArray.getJSONObject(i)
                val keyword = item.optString("keyword", "")
                val score = item.optDouble("score", 0.0).toFloat()
                val root = item.optString("root", "")
                
                results.add(
                    ProductRecognitionResult(
                        productName = keyword,
                        category = root,
                        confidence = score,
                        brand = null
                    )
                )
            }
            
            return results
        } catch (e: Exception) {
            Log.e(TAG, "解析百度 AI 响应失败", e)
            return emptyList()
        }
    }

    /**
     * 生成阿里云签名
     */
    private fun generateAliyunSignature(params: Map<String, String>, secret: String): String {
        // 简化实现，实际需要按照阿里云文档实现完整签名算法
        // 这里返回空字符串，实际使用时需要实现
        return ""
    }

    /**
     * 获取 UTC 时间
     */
    private fun getUTCTime(): String {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date())
    }

    /**
     * 将 Bitmap 转换为 Base64
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}

/**
 * 第三方 API 识别结果
 */
data class ProductRecognitionResult(
    val productName: String,        // 商品名称
    val brand: String? = null,     // 品牌
    val category: String,          // 类别
    val confidence: Float,         // 置信度
    val price: Double? = null,     // 价格（如果有）
    val similarProducts: List<String> = emptyList() // 相似商品
) {
    /**
     * 转换为 RecognitionResult（兼容现有系统）
     */
    fun toRecognitionResult(): RecognitionResult {
        val label = productName + (brand?.let { " $it" } ?: "")
        return RecognitionResult(
            label = label,
            confidence = confidence,
            index = 0
        )
    }
}


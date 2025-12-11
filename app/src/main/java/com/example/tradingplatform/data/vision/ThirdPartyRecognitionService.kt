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
     * 百度 AI 商品识别（高精度）
     * 优先使用商品识别API，如果失败则使用通用识别API
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

            // 2. 优先尝试商品识别 API（更精确）
            val productResults = recognizeWithBaiduProduct(token, imageBase64)
            if (productResults.isNotEmpty()) {
                Log.d(TAG, "使用商品识别API，识别到 ${productResults.size} 个结果")
                return productResults
            }

            // 3. 如果商品识别失败，使用通用识别API
            Log.d(TAG, "商品识别无结果，使用通用识别API")
            val generalResults = recognizeWithBaiduGeneral(token, imageBase64)
            
            // 4. 对通用识别结果进行映射，转换为更具体的商品名称
            return mapGeneralResultsToProducts(generalResults)
        } catch (e: Exception) {
            Log.e(TAG, "百度 AI 识别失败", e)
            return emptyList()
        }
    }

    /**
     * 百度AI商品识别API（高精度，可识别具体商品、品牌、型号）
     */
    private suspend fun recognizeWithBaiduProduct(token: String, imageBase64: String): List<ProductRecognitionResult> {
        try {
            val url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/product?access_token=$token"
            
            val requestBody = "image=$imageBase64".toRequestBody("application/x-www-form-urlencoded".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return emptyList()

            Log.d(TAG, "百度AI商品识别响应: $responseBody")

            // 解析商品识别结果
            val json = JSONObject(responseBody)
            if (json.has("result")) {
                val resultArray = json.optJSONArray("result")
                if (resultArray != null && resultArray.length() > 0) {
                    val results = mutableListOf<ProductRecognitionResult>()
                    // 需要过滤的通用材质/抽象标签
                    val lowPriorityLabels = setOf("金属", "玻璃", "塑料", "材质", "表面", "纹理")
                    
                    for (i in 0 until resultArray.length()) {
                        val item = resultArray.getJSONObject(i)
                        val name = item.optString("name", "")
                        val score = item.optDouble("score", 0.0).toFloat()
                        val baikeInfo = item.optJSONObject("baike_info")
                        val description = baikeInfo?.optString("description", "") ?: ""
                        
                        // 过滤：置信度阈值 > 0.3，且过滤纯材质标签（除非置信度很高）
                        val isLowPriority = lowPriorityLabels.any { name.contains(it) }
                        if (score > 0.3f && (!isLowPriority || score > 0.8f)) {
                            results.add(
                                ProductRecognitionResult(
                                    productName = name,
                                    category = extractCategoryFromDescription(description),
                                    confidence = score,
                                    brand = extractBrandFromName(name)
                                )
                            )
                        }
                    }
                    // 按置信度排序，优先显示具体商品
                    return results.sortedByDescending { it.confidence }
                }
            }
            return emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "商品识别API调用失败，将使用通用识别", e)
            return emptyList()
        }
    }

    /**
     * 百度AI通用识别API（备用方案）
     */
    private suspend fun recognizeWithBaiduGeneral(token: String, imageBase64: String): List<ProductRecognitionResult> {
        val url = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general?access_token=$token"
        
        val requestBody = "image=$imageBase64".toRequestBody("application/x-www-form-urlencoded".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return emptyList()

        Log.d(TAG, "百度AI通用识别响应: $responseBody")

        return parseBaiduResponse(responseBody)
    }

    /**
     * 将通用识别结果映射为具体商品名称
     * 解决"glass"、"metal"等材质识别问题，转换为具体商品
     */
    private fun mapGeneralResultsToProducts(generalResults: List<ProductRecognitionResult>): List<ProductRecognitionResult> {
        // 需要过滤的纯材质标签（除非置信度很高或与其他特征组合）
        val materialOnlyLabels = setOf("metal", "glass", "plastic", "wood", "fabric", "leather", 
            "ceramic", "stone", "aluminum", "steel", "copper", "silver", "gold")
        
        // 先过滤：移除置信度低且是纯材质的标签
        val filteredResults = generalResults.filter { result ->
            val lowerName = result.productName.lowercase()
            val isMaterialOnly = materialOnlyLabels.any { lowerName == it || lowerName.contains("$it ") }
            
            // 保留条件：置信度 > 0.3，且如果是纯材质则置信度必须 > 0.7
            result.confidence > 0.3f && (!isMaterialOnly || result.confidence > 0.7f)
        }
        // 优先级映射：更具体的匹配优先
        val productMapping = listOf(
            // 手机相关（高优先级）
            "smartphone" to "手机",
            "mobile phone" to "手机",
            "cell phone" to "手机",
            "iphone" to "iPhone手机",
            "android phone" to "Android手机",
            
            // 手机特征（中优先级）- 如果识别到这些特征，很可能是手机
            "screen" to "手机",
            "display" to "手机",
            "touchscreen" to "手机",
            "home button" to "手机",
            "camera lens" to "手机",
            "phone case" to "手机",
            
            // 材质特征（低优先级）- 需要结合其他特征判断
            // 如果同时识别到 glass + metal/plastic，很可能是手机
            "glass" to "手机",
            "metal" to "手机",
            "plastic" to "手机",
            "aluminum" to "手机",
            
            // 其他电子产品
            "electronic device" to "电子产品",
            "laptop" to "笔记本电脑",
            "notebook" to "笔记本电脑",
            "computer" to "电脑",
            "desktop" to "台式电脑",
            "tablet" to "平板电脑",
            "ipad" to "iPad",
            "headphone" to "耳机",
            "earphone" to "耳机",
            "earphones" to "耳机",
            "headphones" to "耳机",
            "earbuds" to "无线耳机",
            "speaker" to "音响",
            "camera" to "相机",
            "watch" to "手表",
            "wristwatch" to "手表",
            "timepiece" to "手表",
            "smartwatch" to "智能手表",
            "apple watch" to "Apple Watch",
            "keyboard" to "键盘",
            "mouse" to "鼠标",
            "monitor" to "显示器",
            "tv" to "电视",
            "television" to "电视",
            
            // 其他商品
            "book" to "书籍",
            "clothing" to "服装",
            "shoes" to "鞋子",
            "bag" to "包",
            "furniture" to "家具"
        )

        // 检查是否同时识别到多个手机相关特征（使用原始结果判断）
        val hasPhoneFeatures = filteredResults.any { result ->
            val lower = result.productName.lowercase()
            lower.contains("screen") || lower.contains("display") || 
            lower.contains("touchscreen") || lower.contains("camera lens") ||
            lower.contains("smartphone") || lower.contains("phone")
        }
        
        val hasPhoneMaterials = filteredResults.count { result ->
            val lower = result.productName.lowercase()
            lower.contains("glass") || lower.contains("metal") || 
            lower.contains("plastic") || lower.contains("aluminum")
        } >= 2 // 至少识别到2种材质

        // 添加手表识别映射
        val watchKeywords = setOf("watch", "wristwatch", "timepiece", "clock", "dial", "strap", "band")
        val hasWatchFeatures = filteredResults.any { result ->
            val lower = result.productName.lowercase()
            watchKeywords.any { lower.contains(it) }
        }

        val mappedResults = filteredResults.mapIndexed { index, result ->
            val lowerKeyword = result.productName.lowercase()
            
            // 优先匹配具体商品名称
            var mappedName = productMapping.find { (key, _) ->
                lowerKeyword.contains(key)
            }?.second

            // 如果识别到手机特征组合，且当前结果是材质，则映射为手机
            if (mappedName == null && hasPhoneFeatures && hasPhoneMaterials) {
                if (lowerKeyword.contains("glass") || lowerKeyword.contains("metal") || 
                    lowerKeyword.contains("plastic") || lowerKeyword.contains("aluminum")) {
                    mappedName = "手机"
                }
            }
            
            // 手表识别：如果识别到手表特征，映射为手表
            if (mappedName == null && hasWatchFeatures) {
                if (watchKeywords.any { lowerKeyword.contains(it) }) {
                    mappedName = "手表"
                }
            }
            
            // 如果仍然是材质标签且置信度不高，尝试根据上下文推断
            if (mappedName == null && materialOnlyLabels.any { lowerKeyword == it }) {
                // 如果置信度不高，直接跳过（已在前面过滤）
                // 这里只处理高置信度的材质标签
                if (result.confidence > 0.7f) {
                    // 根据其他识别结果推断
                    if (hasPhoneFeatures) {
                        mappedName = "手机"
                    } else if (hasWatchFeatures) {
                        mappedName = "手表"
                    }
                }
            }

            if (mappedName != null) {
                result.copy(
                    productName = mappedName,
                    category = if (result.category.isEmpty()) {
                        when {
                            mappedName.contains("手机") || mappedName.contains("电脑") || 
                            mappedName.contains("耳机") || mappedName.contains("相机") -> "电子产品"
                            mappedName.contains("书籍") -> "图书文具"
                            mappedName.contains("服装") || mappedName.contains("鞋子") -> "服装配饰"
                            mappedName.contains("家具") -> "家具家电"
                            else -> "其他"
                        }
                    } else {
                        result.category
                    },
                    // 如果是第一个结果且是手机，提高置信度
                    confidence = if (index == 0 && mappedName.contains("手机")) {
                        minOf(0.9f, result.confidence + 0.2f)
                    } else {
                        result.confidence
                    }
                )
            } else {
                result
            }
        }
        
        // 排序：优先显示具体商品（非材质），然后按置信度排序
        return mappedResults
            .filter { it.productName.isNotEmpty() } // 过滤掉空名称
            .sortedWith(compareByDescending<ProductRecognitionResult> { result ->
                val lowerName = result.productName.lowercase()
                val isMaterial = materialOnlyLabels.any { lowerName.contains(it) }
                when {
                    !isMaterial && result.confidence > 0.6f -> 3 // 具体商品且高置信度
                    !isMaterial -> 2 // 具体商品
                    result.confidence > 0.7f -> 1 // 高置信度材质
                    else -> 0
                }
            }.thenByDescending { it.confidence })
            .take(5) // 只返回前5个结果
    }

    /**
     * 从商品名称中提取品牌
     */
    private fun extractBrandFromName(name: String): String? {
        val brands = listOf("iPhone", "Samsung", "Huawei", "Xiaomi", "OPPO", "Vivo", "OnePlus", 
                           "Apple", "华为", "小米", "OPPO", "vivo", "一加", "三星")
        return brands.find { name.contains(it, ignoreCase = true) }
    }

    /**
     * 从描述中提取类别
     */
    private fun extractCategoryFromDescription(description: String): String {
        val categoryKeywords = mapOf(
            "手机" to "电子产品",
            "电脑" to "电子产品",
            "耳机" to "电子产品",
            "相机" to "电子产品",
            "手表" to "电子产品",
            "服装" to "服装配饰",
            "书籍" to "图书文具",
            "家具" to "家具家电"
        )
        
        return categoryKeywords.entries.find { (keyword, _) ->
            description.contains(keyword)
        }?.value ?: "其他"
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


package com.example.tradingplatform.data.vision

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * 图像识别服务
 * 支持设备端和云端两种模式
 */
class ImageRecognitionService(
    private val context: Context? = null,
    private val useCloudModel: Boolean = false // 是否使用云端模型
) {
    companion object {
        private const val TAG = "ImageRecognition"
    }

    // 根据配置选择设备端或云端模型
    // 注意：ML Kit Image Labeling 默认使用设备端模型
    // 云端模型需要 Firebase 配置和不同的 API，这里统一使用设备端模型
    // 云端识别通过第三方 API（百度 AI、阿里云等）实现
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    /**
     * 检查网络是否可用
     */
    private fun isNetworkAvailable(): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * 识别图片中的物体
     * @param bitmap 图片位图
     * @return 识别结果列表（按置信度排序）
     */
    suspend fun recognizeImage(bitmap: Bitmap): List<RecognitionResult> = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            
            suspendCancellableCoroutine { continuation ->
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        // 需要过滤的通用材质/抽象标签（低优先级）
                        val lowPriorityLabels = setOf(
                            "metal", "glass", "plastic", "wood", "fabric", "leather",
                            "ceramic", "stone", "paper", "cardboard", "foam",
                            "surface", "material", "texture", "pattern", "color"
                        )
                        
                        val results = labels.map { label ->
                            RecognitionResult(
                                label = label.text,
                                confidence = label.confidence,
                                index = label.index
                            )
                        }
                        .filter { result ->
                            // 1. 置信度阈值：只保留置信度 > 0.3 的结果
                            result.confidence > 0.3f
                        }
                        .filter { result ->
                            // 2. 过滤通用材质标签（除非置信度很高 > 0.8）
                            val isLowPriority = lowPriorityLabels.any { 
                                result.label.lowercase().contains(it) 
                            }
                            !isLowPriority || result.confidence > 0.8f
                        }
                        .sortedWith(compareByDescending<RecognitionResult> { result ->
                            // 优先排序：具体商品名称 > 高置信度 > 其他
                            val isSpecificProduct = !lowPriorityLabels.any { 
                                result.label.lowercase().contains(it) 
                            }
                            when {
                                isSpecificProduct && result.confidence > 0.6f -> 3
                                isSpecificProduct -> 2
                                result.confidence > 0.7f -> 1
                                else -> 0
                            }
                        }.thenByDescending { it.confidence })
                        
                        val modelType = if (useCloudModel) "云端" else "设备端"
                        Log.d(TAG, "$modelType 识别成功: ${results.size} 个标签（已过滤）")
                        continuation.resume(results)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "识别失败", e)
                        continuation.resume(emptyList())
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理图片失败", e)
            emptyList()
        }
    }

    /**
     * 关闭识别器
     */
    fun close() {
        labeler.close()
    }
}

/**
 * 识别结果
 */
data class RecognitionResult(
    val label: String, // 标签名称（英文）
    val confidence: Float, // 置信度（0-1）
    val index: Int // 标签索引
) {
    /**
     * 转换为中文类别
     */
    fun toCategory(): String {
        // 简单的英文到中文映射
        val categoryMap = mapOf(
            "headphone" to "电子产品",
            "earphone" to "电子产品",
            "earphones" to "电子产品",
            "headphones" to "电子产品",
            "laptop" to "电子产品",
            "computer" to "电子产品",
            "phone" to "电子产品",
            "smartphone" to "电子产品",
            "mobile phone" to "电子产品",
            "book" to "图书文具",
            "books" to "图书文具",
            "clothing" to "服装配饰",
            "clothes" to "服装配饰",
            "shirt" to "服装配饰",
            "pants" to "服装配饰",
            "shoe" to "服装配饰",
            "shoes" to "服装配饰",
            "glasses" to "服装配饰",
            "eyeglasses" to "服装配饰",
            "sunglasses" to "服装配饰",
            "furniture" to "家具家电",
            "chair" to "家具家电",
            "table" to "家具家电",
            "sofa" to "家具家电",
            "curtain" to "家具家电",
            "curtains" to "家具家电",
            "bed" to "家具家电",
            "lamp" to "家具家电",
            "sports" to "运动健身",
            "bicycle" to "运动健身",
            "bike" to "运动健身",
            "cosmetics" to "美妆护肤",
            "makeup" to "美妆护肤",
            "food" to "食品饮料",
            "drink" to "食品饮料",
            "toy" to "玩具模型",
            "toys" to "玩具模型",
            "car" to "汽车用品",
            "vehicle" to "汽车用品",
            "smile" to "其他",
            "person" to "其他",
            "face" to "其他"
        )
        
        val lowerLabel = label.lowercase()
        return categoryMap.entries.find { (key, _) ->
            lowerLabel.contains(key)
        }?.value ?: "其他"
    }
    
    /**
     * 转换为中文关键词
     */
    fun toChineseKeyword(): String {
        val keywordMap = mapOf(
            "headphone" to "耳机",
            "earphone" to "耳机",
            "earphones" to "耳机",
            "headphones" to "耳机",
            "laptop" to "笔记本电脑",
            "computer" to "电脑",
            "phone" to "手机",
            "smartphone" to "智能手机",
            "mobile phone" to "手机",
            "book" to "书籍",
            "books" to "书籍",
            "clothing" to "服装",
            "clothes" to "衣服",
            "shirt" to "衬衫",
            "pants" to "裤子",
            "shoe" to "鞋子",
            "shoes" to "鞋子",
            "chair" to "椅子",
            "table" to "桌子",
            "sofa" to "沙发",
            "bicycle" to "自行车",
            "bike" to "自行车",
            "car" to "汽车",
            "glasses" to "眼镜",
            "eyeglasses" to "眼镜",
            "sunglasses" to "太阳镜",
            "curtain" to "窗帘",
            "curtains" to "窗帘",
            "smile" to "微笑",
            "person" to "人物",
            "face" to "人脸",
            "wall" to "墙壁",
            "furniture" to "家具",
            "window" to "窗户",
            "door" to "门",
            "bed" to "床",
            "pillow" to "枕头",
            "lamp" to "台灯",
            "clock" to "时钟",
            "picture" to "图片",
            "frame" to "相框"
        )
        
        val lowerLabel = label.lowercase()
        return keywordMap.entries.find { (key, _) ->
            lowerLabel.contains(key)
        }?.value ?: label
    }
}


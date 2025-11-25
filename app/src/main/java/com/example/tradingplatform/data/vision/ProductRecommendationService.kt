package com.example.tradingplatform.data.vision

import android.content.Context
import android.util.Log
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.data.items.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 商品推荐服务
 * 根据图像识别结果推荐相关商品
 */
class ProductRecommendationService(
    private val context: Context
) {
    companion object {
        private const val TAG = "ProductRecommendation"
    }

    private val itemRepository = ItemRepository(context)

    /**
     * 根据识别结果推荐商品
     * @param recognitionResults 识别结果列表
     * @return 推荐的商品列表（按匹配度排序）
     */
    suspend fun recommendProducts(recognitionResults: List<RecognitionResult>): List<RecommendedProduct> = withContext(Dispatchers.IO) {
        if (recognitionResults.isEmpty()) {
            return@withContext emptyList()
        }

        // 获取所有商品
        val allItems = itemRepository.listItems()
        if (allItems.isEmpty()) {
            return@withContext emptyList()
        }

        // 提取识别关键词和类别
        val categories = recognitionResults.map { it.toCategory() }.distinct()
        val keywords = recognitionResults.map { it.toChineseKeyword() }.distinct()
        val topResult = recognitionResults.firstOrNull()

        Log.d(TAG, "识别类别: $categories")
        Log.d(TAG, "识别关键词: $keywords")

        // 计算每个商品的匹配分数
        val scoredItems = allItems.map { item ->
            val score = calculateMatchScore(item, categories, keywords, topResult)
            RecommendedProduct(item, score, getMatchReasons(item, categories, keywords))
        }.filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(10) // 返回前10个推荐

        Log.d(TAG, "推荐了 ${scoredItems.size} 个商品")
        scoredItems
    }

    /**
     * 计算商品匹配分数
     */
    private fun calculateMatchScore(
        item: Item,
        categories: List<String>,
        keywords: List<String>,
        topResult: RecognitionResult?
    ): Float {
        var score = 0f

        // 1. 类别匹配（40分）
        if (item.category.isNotEmpty() && categories.contains(item.category)) {
            score += 40f
        }

        // 2. 标题关键词匹配（50分）
        val titleLower = item.title.lowercase()
        keywords.forEach { keyword ->
            if (titleLower.contains(keyword.lowercase())) {
                score += 50f / keywords.size // 平均分配分数
            }
        }

        // 3. 描述匹配（10分）
        val descLower = item.description.lowercase()
        keywords.forEach { keyword ->
            if (descLower.contains(keyword.lowercase())) {
                score += 10f / keywords.size
            }
        }

        // 4. 置信度加权（如果置信度高，增加分数）
        if (topResult != null && topResult.confidence > 0.7f) {
            score *= 1.2f
        }

        return score.coerceIn(0f, 100f)
    }

    /**
     * 获取匹配原因
     */
    private fun getMatchReasons(
        item: Item,
        categories: List<String>,
        keywords: List<String>
    ): List<String> {
        val reasons = mutableListOf<String>()

        if (item.category.isNotEmpty() && categories.contains(item.category)) {
            reasons.add("类别匹配")
        }

        val titleLower = item.title.lowercase()
        keywords.forEach { keyword ->
            if (titleLower.contains(keyword.lowercase())) {
                reasons.add("标题包含「$keyword」")
            }
        }

        return reasons.distinct()
    }
}

/**
 * 推荐商品
 */
data class RecommendedProduct(
    val item: Item,
    val score: Float, // 匹配分数（0-100）
    val matchReasons: List<String> // 匹配原因
)



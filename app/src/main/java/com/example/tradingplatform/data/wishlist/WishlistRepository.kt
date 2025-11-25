package com.example.tradingplatform.data.wishlist

import android.content.Context
import android.util.Log
import com.example.tradingplatform.data.auth.AuthRepository
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.data.items.ItemRepository
import com.example.tradingplatform.data.local.AppDatabase
import com.example.tradingplatform.data.local.WishlistDao
import com.example.tradingplatform.data.local.WishlistEntity
import com.example.tradingplatform.data.supabase.SupabaseApi
import com.example.tradingplatform.data.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class WishlistRepository(
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "WishlistRepository"
    }

    private val database: AppDatabase? = context?.let { AppDatabase.getDatabase(it) }
    private val wishlistDao: WishlistDao? = database?.wishlistDao()
    private val authRepo: AuthRepository? = context?.let { AuthRepository(it) }
    private val itemRepository: ItemRepository? = context?.let { ItemRepository(it) }
    private val supabaseApi: SupabaseApi? = context?.let { SupabaseClient.getApi() }

    /**
     * 检查价格变化并发送提醒
     */
    suspend fun checkPriceAlerts() = withContext(Dispatchers.IO) {
        try {
            val wishlist = getWishlistSync()
            if (wishlist.isNotEmpty()) {
                val alertService = PriceAlertService(context ?: return@withContext)
                alertService.checkPriceAlerts(wishlist)
            }
        } catch (e: Exception) {
            Log.e(TAG, "检查价格提醒失败", e)
        }
    }

    /**
     * 添加愿望清单项
     */
    suspend fun addWishlistItem(item: WishlistItem): String = withContext(Dispatchers.IO) {
        if (wishlistDao == null) {
            throw IllegalStateException("数据库未初始化")
        }

        val currentEmail = authRepo?.getCurrentUserEmail() ?: "dev@example.com"
        val currentUid = authRepo?.getCurrentUserUid() ?: run {
            // 如果获取不到用户ID，使用固定的 dev_user，确保添加和查询时使用相同的ID
            Log.w(TAG, "无法获取用户ID，使用默认 dev_user")
            "dev_user"
        }
        Log.d(TAG, "添加愿望清单项，用户ID: $currentUid, email: $currentEmail")

        // 检查是否已经存在相同的 itemId（如果提供了 itemId）
        if (item.itemId.isNotEmpty()) {
            val existing = wishlistDao.getWishlistByUserSync(currentUid)
                .firstOrNull { it.itemId == item.itemId }
            if (existing != null) {
                Log.d(TAG, "愿望清单项已存在，更新: ${existing.id}")
                // 更新现有项
                val updatedItem = item.copy(
                    id = existing.id,
                    userId = currentUid,
                    userEmail = currentEmail,
                    createdAt = Date(existing.createdAt),
                    updatedAt = Date()
                )
                val entity = WishlistEntity.fromWishlistItem(updatedItem)
                wishlistDao.insertWishlistItem(entity)
                return@withContext updatedItem.id
            }
        }

        val newItem = item.copy(
            id = if (item.id.isEmpty()) UUID.randomUUID().toString() else item.id,
            userId = currentUid,
            userEmail = currentEmail,
            createdAt = Date(),
            updatedAt = Date()
        )

        val entity = WishlistEntity.fromWishlistItem(newItem)
        wishlistDao.insertWishlistItem(entity)
        Log.d(TAG, "愿望清单项已添加到本地: ${newItem.id}, title: ${newItem.title}, itemId: ${newItem.itemId}")
        
        // 同步到 Supabase
        try {
            val supabaseRequest = com.example.tradingplatform.data.supabase.SupabaseWishlistItem.fromWishlistItem(newItem)
            val response = supabaseApi?.createWishlistItem(supabaseRequest)
            if (response?.isSuccessful == true) {
                Log.d(TAG, "愿望清单项已同步到 Supabase: ${newItem.id}")
            } else {
                val errorBody = response?.errorBody()?.string()
                Log.w(TAG, "Supabase 同步失败: HTTP ${response?.code()} - $errorBody")
                // 即使 Supabase 失败，本地数据仍然保存
            }
        } catch (e: Exception) {
            Log.w(TAG, "Supabase 同步失败（本地数据已保存）", e)
            // 即使 Supabase 失败，本地数据仍然保存
        }
        
        newItem.id
    }

    /**
     * 删除愿望清单项
     */
    suspend fun deleteWishlistItem(itemId: String) = withContext(Dispatchers.IO) {
        if (wishlistDao == null) {
            throw IllegalStateException("数据库未初始化")
        }
        wishlistDao.deleteWishlistItemById(itemId)
        Log.d(TAG, "愿望清单项已从本地删除: $itemId")
        
        // 同步删除 Supabase
        try {
            val response = supabaseApi?.deleteWishlistItem("eq.$itemId")
            if (response?.isSuccessful == true) {
                Log.d(TAG, "愿望清单项已从 Supabase 删除: $itemId")
            } else {
                val errorBody = response?.errorBody()?.string()
                Log.w(TAG, "Supabase 删除失败: HTTP ${response?.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Supabase 删除失败（本地已删除）", e)
        }
    }

    /**
     * 获取当前用户的愿望清单
     */
    fun getWishlistFlow(): Flow<List<WishlistItem>> {
        if (wishlistDao == null) {
            Log.w(TAG, "wishlistDao 为 null，返回空列表")
            return flowOf(emptyList())
        }

        // 使用 flow 构建器，在 flowOn 中执行 suspend 函数
        return flow {
            try {
                val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
                Log.d(TAG, "获取愿望清单 Flow，用户ID: $currentUid")
                emit(currentUid)
            } catch (e: Exception) {
                Log.e(TAG, "获取用户ID失败", e)
                emit("dev_user") // 降级到默认用户ID
            }
        }.flowOn(Dispatchers.IO)
        .flatMapLatest { userId ->
            try {
                wishlistDao.getWishlistByUser(userId).map { entities ->
                    val items = entities.map { 
                        try {
                            it.toWishlistItem()
                        } catch (e: Exception) {
                            Log.e(TAG, "转换愿望清单项失败", e)
                            null
                        }
                    }.filterNotNull()
                    Log.d(TAG, "愿望清单 Flow 更新，用户ID: $userId, 数量: ${items.size}")
                    items
                }
            } catch (e: Exception) {
                Log.e(TAG, "查询愿望清单失败", e)
                flowOf(emptyList())
            }
        }
    }

    /**
     * 获取当前用户的愿望清单（同步）
     */
    suspend fun getWishlistSync(): List<WishlistItem> = withContext(Dispatchers.IO) {
        if (wishlistDao == null) {
            return@withContext emptyList()
        }
        val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
        wishlistDao.getWishlistByUserSync(currentUid).map { it.toWishlistItem() }
    }

    /**
     * 获取所有愿望清单（用于匹配）
     * 优先从 Supabase 获取，失败则从本地获取
     */
    suspend fun getAllWishlistItems(): List<WishlistItem> = withContext(Dispatchers.IO) {
        // 优先从 Supabase 获取（用于反向匹配）
        try {
            val response = supabaseApi?.getAllWishlistItems(100)
            if (response?.isSuccessful == true) {
                val supabaseItems = response.body() ?: emptyList()
                if (supabaseItems.isNotEmpty()) {
                    Log.d(TAG, "从 Supabase 获取到 ${supabaseItems.size} 个愿望清单项")
                    // 同步到本地数据库
                    supabaseItems.forEach { supabaseItem ->
                        try {
                            val localItem = supabaseItem.toWishlistItem()
                            val entity = WishlistEntity.fromWishlistItem(localItem)
                            wishlistDao?.insertWishlistItem(entity)
                        } catch (e: Exception) {
                            Log.w(TAG, "同步愿望清单项到本地失败: ${supabaseItem.id}", e)
                        }
                    }
                    return@withContext supabaseItems.map { it.toWishlistItem() }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "从 Supabase 获取愿望清单失败，使用本地数据", e)
        }
        
        // 从本地数据库获取（降级方案）
        if (wishlistDao == null) {
            return@withContext emptyList()
        }
        val localItems = wishlistDao.getAllWishlistItems().map { it.toWishlistItem() }
        Log.d(TAG, "从本地获取到 ${localItems.size} 个愿望清单项")
        localItems
    }

    /**
     * 智能匹配交换机会
     * 根据用户的愿望清单，匹配可交换的商品
     * 
     * @param minScore 最低匹配分数（默认30）
     * @param maxResults 最大返回结果数（默认50，0表示不限制）
     */
    suspend fun findExchangeMatches(
        minScore: Double = 30.0,
        maxResults: Int = 50
    ): List<ExchangeMatch> = withContext(Dispatchers.IO) {
        val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
        
        // 获取当前用户的愿望清单
        val wishlist = getWishlistSync()
        if (wishlist.isEmpty()) {
            Log.d(TAG, "愿望清单为空，无法匹配")
            return@withContext emptyList()
        }

        // 获取当前用户发布的商品（用于交换）
        val myItems = itemRepository?.listItems()?.filter { it.ownerUid == currentUid } ?: emptyList()

        // 获取所有其他用户的愿望清单
        val allWishlist = getAllWishlistItems().filter { it.userId != currentUid }

        // 获取所有其他用户发布的商品
        val allItems = itemRepository?.listItems()?.filter { it.ownerUid != currentUid } ?: emptyList()

        if (allItems.isEmpty() && myItems.isEmpty()) {
            Log.d(TAG, "没有可匹配的商品")
            return@withContext emptyList()
        }

        val matches = mutableListOf<ExchangeMatch>()

        // 匹配逻辑1：我的愿望清单 vs 其他用户的商品（正向匹配）
        for (wish in wishlist) {
            for (item in allItems) {
                val score = calculateMatchScore(wish, item)
                if (score >= minScore) {
                    val reasons = getMatchReasons(wish, item, score)
                    matches.add(ExchangeMatch(wish, item, score, reasons, isReverseMatch = false))
                }
            }
        }

        // 匹配逻辑2：其他用户的愿望清单 vs 我的商品（反向匹配）
        // 只有在用户有发布的商品时才进行反向匹配
        if (myItems.isNotEmpty()) {
            for (wish in allWishlist) {
                for (item in myItems) {
                    val score = calculateMatchScore(wish, item)
                    if (score >= minScore) {
                        val reasons = getMatchReasons(wish, item, score)
                        matches.add(ExchangeMatch(wish, item, score, reasons, isReverseMatch = true))
                    }
                }
            }
        }

        // 按匹配分数排序
        val sortedMatches = matches.sortedByDescending { it.matchScore }
        
        // 限制返回结果数
        if (maxResults > 0 && sortedMatches.size > maxResults) {
            return@withContext sortedMatches.take(maxResults)
        }
        
        return@withContext sortedMatches
    }

    /**
     * 匹配单个愿望清单项
     * 只匹配该愿望清单项与其他用户发布的商品
     * 
     * @param wishlistItemId 愿望清单项ID
     * @param minScore 最低匹配分数（默认30）
     * @param maxResults 最大返回结果数（默认20，0表示不限制）
     */
    suspend fun findMatchesForWishlistItem(
        wishlistItemId: String,
        minScore: Double = 30.0,
        maxResults: Int = 20
    ): List<ExchangeMatch> = withContext(Dispatchers.IO) {
        val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
        
        // 获取指定的愿望清单项
        val wishlist = getWishlistSync()
        val targetWish = wishlist.firstOrNull { it.id == wishlistItemId }
        
        if (targetWish == null) {
            Log.d(TAG, "未找到愿望清单项: $wishlistItemId")
            return@withContext emptyList()
        }

        // 获取所有其他用户发布的商品
        val allItems = itemRepository?.listItems()?.filter { it.ownerUid != currentUid } ?: emptyList()

        if (allItems.isEmpty()) {
            Log.d(TAG, "没有可匹配的商品")
            return@withContext emptyList()
        }

        val matches = mutableListOf<ExchangeMatch>()

        // 只进行正向匹配：该愿望清单项 vs 其他用户的商品
        for (item in allItems) {
            val score = calculateMatchScore(targetWish, item)
            if (score >= minScore) {
                val reasons = getMatchReasons(targetWish, item, score)
                matches.add(ExchangeMatch(targetWish, item, score, reasons, isReverseMatch = false))
            }
        }

        // 按匹配分数排序
        val sortedMatches = matches.sortedByDescending { it.matchScore }
        
        // 限制返回结果数
        if (maxResults > 0 && sortedMatches.size > maxResults) {
            return@withContext sortedMatches.take(maxResults)
        }
        
        return@withContext sortedMatches
    }

    /**
     * 计算匹配分数（0-100）
     */
    private fun calculateMatchScore(wish: WishlistItem, item: Item): Double {
        var score = 0.0

        // 1. 标题关键词匹配（40分）
        val titleMatch = calculateTextSimilarity(wish.title.lowercase(), item.title.lowercase())
        score += titleMatch * 40

        // 2. 描述匹配（20分）
        val descMatch = calculateTextSimilarity(
            wish.description.lowercase(),
            item.description.lowercase()
        )
        score += descMatch * 20

        // 3. 价格匹配（30分）
        val priceScore = calculatePriceMatch(wish, item)
        score += priceScore * 30

        // 4. 类别匹配（10分，如果有类别）
        if (wish.category.isNotEmpty()) {
            val categoryMatch = when {
                item.category.isNotEmpty() && item.category == wish.category -> 1.0 // 完全匹配
                item.category.isNotEmpty() && item.category.contains(wish.category) -> 0.7 // 包含匹配
                item.title.lowercase().contains(wish.category.lowercase()) ||
                item.description.lowercase().contains(wish.category.lowercase()) -> 0.5 // 文本匹配
                else -> 0.0
            }
            score += categoryMatch * 10
        } else {
            score += 10 // 如果没有类别，给满分
        }

        return score.coerceIn(0.0, 100.0)
    }

    /**
     * 计算文本相似度（0-1）
     * 使用改进的算法，支持中文分词
     */
    private fun calculateTextSimilarity(text1: String, text2: String): Double {
        if (text1.isEmpty() || text2.isEmpty()) return 0.0

        // 支持中英文混合的关键词匹配
        // 中文按字符分割，英文按空格分割
        val words1 = extractKeywords(text1)
        val words2 = extractKeywords(text2)

        if (words1.isEmpty() || words2.isEmpty()) return 0.0

        // 计算交集
        var matches = 0
        val matchedWords = mutableSetOf<String>()
        
        for (word1 in words1) {
            for (word2 in words2) {
                if (word1 == word2 || 
                    word1.contains(word2) || 
                    word2.contains(word1) ||
                    calculateLevenshteinSimilarity(word1, word2) > 0.7) {
                    if (word1 !in matchedWords) {
                        matches++
                        matchedWords.add(word1)
                    }
                    break
                }
            }
        }

        // 计算并集相似度（Jaccard相似度）
        val union = (words1 + words2).distinct().size
        val jaccard = if (union > 0) matches.toDouble() / union else 0.0
        
        // 计算交集与较小集合的比例
        val minSize = minOf(words1.size, words2.size)
        val intersectionRatio = if (minSize > 0) matches.toDouble() / minSize else 0.0
        
        // 综合两种相似度
        return ((jaccard * 0.4 + intersectionRatio * 0.6)).coerceIn(0.0, 1.0)
    }
    
    /**
     * 提取关键词（支持中英文）
     */
    private fun extractKeywords(text: String): List<String> {
        val keywords = mutableListOf<String>()
        
        // 移除标点符号和多余空格
        val cleaned = text.replace(Regex("[，。！？、；：\"\"''（）【】《》\\s]+"), " ")
        
        // 按空格分割（英文单词）
        val parts = cleaned.split(" ").filter { it.isNotBlank() }
        
        for (part in parts) {
            // 如果是中文（长度>=2），按字符分割
            if (part.length >= 2 && part.all { it.code >= 0x4E00 && it.code <= 0x9FFF }) {
                // 中文：提取2-4字的词组
                for (i in 0 until part.length - 1) {
                    for (len in 2..minOf(4, part.length - i)) {
                        if (i + len <= part.length) {
                            keywords.add(part.substring(i, i + len))
                        }
                    }
                }
            } else {
                // 英文：过滤掉太短的词
                if (part.length >= 2) {
                    keywords.add(part.lowercase())
                }
            }
        }
        
        return keywords.distinct()
    }
    
    /**
     * 计算编辑距离相似度（Levenshtein距离）
     */
    private fun calculateLevenshteinSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0
        
        val maxLen = maxOf(s1.length, s2.length)
        val distance = levenshteinDistance(s1, s2)
        return 1.0 - (distance.toDouble() / maxLen)
    }
    
    /**
     * 计算Levenshtein距离
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // 删除
                    dp[i][j - 1] + 1,      // 插入
                    dp[i - 1][j - 1] + cost // 替换
                )
            }
        }
        
        return dp[m][n]
    }

    /**
     * 计算价格匹配分数（0-1）
     */
    private fun calculatePriceMatch(wish: WishlistItem, item: Item): Double {
        val itemPrice = item.price

        // 如果愿望清单没有价格限制，给中等分数
        if (wish.minPrice == 0.0 && wish.maxPrice == 0.0) {
            return 0.6
        }

        // 检查价格是否在范围内
        val inRange = (wish.minPrice == 0.0 || itemPrice >= wish.minPrice) &&
                (wish.maxPrice == 0.0 || itemPrice <= wish.maxPrice)

        if (inRange) {
            // 在范围内，计算价格接近度
            val midPrice = if (wish.minPrice > 0 && wish.maxPrice > 0) {
                (wish.minPrice + wish.maxPrice) / 2
            } else if (wish.minPrice > 0) {
                wish.minPrice * 1.5
            } else {
                wish.maxPrice * 0.7
            }

            val diff = kotlin.math.abs(itemPrice - midPrice)
            val maxDiff = midPrice * 0.5
            return (1.0 - (diff / maxDiff).coerceIn(0.0, 1.0))
        }

        // 不在范围内，但价格接近，给部分分数
        val range = if (wish.minPrice > 0 && wish.maxPrice > 0) {
            wish.maxPrice - wish.minPrice
        } else if (wish.minPrice > 0) {
            wish.minPrice * 0.5
        } else {
            wish.maxPrice * 0.3
        }

        val distance = if (wish.minPrice > 0 && itemPrice < wish.minPrice) {
            wish.minPrice - itemPrice
        } else if (wish.maxPrice > 0 && itemPrice > wish.maxPrice) {
            itemPrice - wish.maxPrice
        } else {
            0.0
        }

        if (distance < range) {
            return 0.3 * (1.0 - distance / range)
        }

        return 0.0
    }

    /**
     * 获取匹配原因
     */
    private fun getMatchReasons(wish: WishlistItem, item: Item, score: Double): List<String> {
        val reasons = mutableListOf<String>()

        if (score > 70) {
            reasons.add("高度匹配")
        } else if (score > 50) {
            reasons.add("良好匹配")
        } else {
            reasons.add("可能匹配")
        }

        // 价格匹配
        val priceMatch = calculatePriceMatch(wish, item)
        if (priceMatch > 0.7) {
            reasons.add("价格合适")
        }

        // 标题匹配
        val titleMatch = calculateTextSimilarity(wish.title.lowercase(), item.title.lowercase())
        if (titleMatch > 0.5) {
            reasons.add("商品类型相似")
        }

        // 类别匹配
        if (wish.category.isNotEmpty() && item.category.isNotEmpty()) {
            if (item.category == wish.category) {
                reasons.add("类别完全匹配")
            } else if (item.category.contains(wish.category) || wish.category.contains(item.category)) {
                reasons.add("类别相关")
            }
        }

        return reasons
    }
}


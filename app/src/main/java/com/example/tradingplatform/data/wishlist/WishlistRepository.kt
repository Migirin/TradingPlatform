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
     * 检查价格变化并发送提醒 / Check price changes and send alerts
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
     * 检查价格变化并返回提醒消息（用于浮窗显示）/ Check price changes and return alert messages (for snackbar display)
     * @param messageFormat 消息格式，包含3个%s占位符：商品名称、现价、目标价 / Message format with 3 %s placeholders: item title, current price, target price
     */
    suspend fun checkPriceAlertsWithResult(messageFormat: String = "🎉「%s」降价啦！现价 ¥%s，低于目标价 ¥%s"): List<String> = withContext(Dispatchers.IO) {
        val alertMessages = mutableListOf<String>()
        try {
            val wishlist = getWishlistSync()
            if (wishlist.isEmpty()) {
                return@withContext alertMessages
            }
            
            val alertService = PriceAlertService(context ?: return@withContext alertMessages)
            
            for (wish in wishlist) {
                if (!wish.enablePriceAlert || wish.targetPrice <= 0) {
                    continue
                }
                
                // 如果有关联的商品ID，检查该商品的价格 / If linked to an item, check its price
                if (wish.itemId.isNotEmpty()) {
                    val item = itemRepository?.getItemById(wish.itemId)
                    if (item != null && item.price <= wish.targetPrice) {
                        val message = String.format(messageFormat, item.title, String.format("%.2f", item.price), String.format("%.2f", wish.targetPrice))
                        alertMessages.add(message)
                        // 同时发送系统通知 / Also send system notification
                        alertService.sendPriceAlertForItem(wish, item)
                    }
                } else {
                    // 如果没有关联商品ID，查找匹配的商品 / If no linked item, find matching items
                    val allItems = itemRepository?.listItems() ?: emptyList()
                    val matchingItems = allItems.filter { item ->
                        val titleMatch = item.title.contains(wish.title, ignoreCase = true) ||
                                wish.title.contains(item.title, ignoreCase = true)
                        val categoryMatch = wish.category.isEmpty() || item.category == wish.category
                        val priceMatch = item.price <= wish.targetPrice
                        titleMatch && categoryMatch && priceMatch
                    }
                    
                    matchingItems.forEach { item ->
                        val message = String.format(messageFormat, item.title, String.format("%.2f", item.price), String.format("%.2f", wish.targetPrice))
                        alertMessages.add(message)
                        alertService.sendPriceAlertForItem(wish, item)
                    }
                }
            }
            
            Log.d(TAG, "检查价格提醒完成，发现 ${alertMessages.size} 个降价商品")
        } catch (e: Exception) {
            Log.e(TAG, "检查价格提醒失败", e)
        }
        alertMessages
    }

    /**
     * 添加愿望清单项 / Add wishlist item
     */
    suspend fun addWishlistItem(item: WishlistItem): String = withContext(Dispatchers.IO) {
        if (wishlistDao == null) {
            throw IllegalStateException("数据库未初始化")
        }

        val currentEmail = authRepo?.getCurrentUserEmail() ?: "dev@example.com"
        val currentUid = authRepo?.getCurrentUserUid() ?: run {
            // 如果获取不到用户ID，使用固定的 dev_user，确保添加和查询时使用相同的ID / If can't get user ID, use fixed dev_user to ensure same ID for add and query
            Log.w(TAG, "无法获取用户ID，使用默认 dev_user")
            "dev_user"
        }
        Log.d(TAG, "添加愿望清单项，用户ID: $currentUid, email: $currentEmail")

        // 检查是否已经存在相同的 itemId（如果提供了 itemId）/ Check if same itemId already exists (if itemId provided)
        if (item.itemId.isNotEmpty()) {
            val existing = wishlistDao.getWishlistByUserSync(currentUid)
                .firstOrNull { it.itemId == item.itemId }
            if (existing != null) {
                Log.d(TAG, "愿望清单项已存在，更新: ${existing.id}")
                // 更新现有项 / Update existing item
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
        
        // 同步到 Supabase / Sync to Supabase
        try {
            val supabaseRequest = com.example.tradingplatform.data.supabase.SupabaseWishlistItem.fromWishlistItem(newItem)
            val response = supabaseApi?.createWishlistItem(supabaseRequest)
            if (response?.isSuccessful == true) {
                Log.d(TAG, "愿望清单项已同步到 Supabase: ${newItem.id}")
            } else {
                val errorBody = response?.errorBody()?.string()
                Log.w(TAG, "Supabase 同步失败: HTTP ${response?.code()} - $errorBody")
                // 即使 Supabase 失败，本地数据仍然保存 / Even if Supabase fails, local data is still saved
            }
        } catch (e: Exception) {
            Log.w(TAG, "Supabase 同步失败（本地数据已保存）", e)
            // 即使 Supabase 失败，本地数据仍然保存 / Even if Supabase fails, local data is still saved
        }
        
        newItem.id
    }

    /**
     * 更新愿望清单项 / Update wishlist item
     */
    suspend fun updateWishlistItem(item: WishlistItem) = withContext(Dispatchers.IO) {
        if (wishlistDao == null) {
            throw IllegalStateException("数据库未初始化")
        }

        val currentEmail = authRepo?.getCurrentUserEmail() ?: "dev@example.com"
        val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
        
        val updatedItem = item.copy(
            userId = currentUid,
            userEmail = currentEmail,
            updatedAt = Date()
        )

        val entity = WishlistEntity.fromWishlistItem(updatedItem)
        wishlistDao.insertWishlistItem(entity) // Room 的 insert 会更新已存在的项
        Log.d(TAG, "愿望清单项已更新到本地: ${updatedItem.id}, title: ${updatedItem.title}")
        
        // 同步到 Supabase / Sync to Supabase
        try {
            val updateRequest = com.example.tradingplatform.data.supabase.UpdateWishlistItemRequest(
                title = updatedItem.title,
                category = updatedItem.category.ifEmpty { null },
                minPrice = updatedItem.minPrice.takeIf { it > 0 },
                maxPrice = updatedItem.maxPrice.takeIf { it > 0 },
                targetPrice = updatedItem.targetPrice.takeIf { it > 0 },
                itemId = updatedItem.itemId.ifEmpty { null },
                enablePriceAlert = updatedItem.enablePriceAlert,
                description = updatedItem.description.ifEmpty { null }
            )
            val response = supabaseApi?.updateWishlistItem("eq.${updatedItem.id}", updateRequest)
            if (response?.isSuccessful == true) {
                Log.d(TAG, "愿望清单项已同步到 Supabase: ${updatedItem.id}")
            } else {
                val errorBody = response?.errorBody()?.string()
                Log.w(TAG, "Supabase 同步失败: HTTP ${response?.code()} - $errorBody")
                // 即使 Supabase 失败，本地数据仍然保存 / Even if Supabase fails, local data is still saved
            }
        } catch (e: Exception) {
            Log.w(TAG, "Supabase 同步失败（本地数据已保存）", e)
            // 即使 Supabase 失败，本地数据仍然保存 / Even if Supabase fails, local data is still saved
        }
    }

    /**
     * 删除愿望清单项 / Delete wishlist item
     */
    suspend fun deleteWishlistItem(itemId: String) = withContext(Dispatchers.IO) {
        if (wishlistDao == null) {
            throw IllegalStateException("数据库未初始化")
        }
        wishlistDao.deleteWishlistItemById(itemId)
        Log.d(TAG, "愿望清单项已从本地删除: $itemId")
        
        // 同步删除 Supabase / Sync delete to Supabase
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
     * 获取当前用户的愿望清单 / Get current user's wishlist
     * 优先从 Supabase 获取，失败则从本地获取 / Prioritize Supabase, fallback to local
     */
    fun getWishlistFlow(): Flow<List<WishlistItem>> {
        Log.d(TAG, "========== getWishlistFlow() 被调用 ==========")
        if (wishlistDao == null) {
            Log.w(TAG, "❌ wishlistDao 为 null，返回空列表")
            return flowOf(emptyList())
        }
        Log.d(TAG, "✅ wishlistDao 不为 null，继续执行")

        // 使用 flow 构建器，在 flowOn 中执行 suspend 函数 / Use flow builder, execute suspend function in flowOn
        return flow {
            try {
                val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
                val currentEmail = authRepo?.getCurrentUserEmail() ?: ""
                Log.d(TAG, "📋 获取愿望清单 Flow，用户ID: $currentUid, email: $currentEmail")
                
                // 先尝试从 Supabase 获取 / Try to get from Supabase first
                try {
                    Log.d(TAG, "🌐 尝试从 Supabase 获取愿望清单...")
                    val response = supabaseApi?.getWishlistByUser("eq.$currentUid")
                    if (response?.isSuccessful == true) {
                        val supabaseItems = response.body() ?: emptyList()
                        if (supabaseItems.isNotEmpty()) {
                            Log.d(TAG, "✅ 从 Supabase 获取到 ${supabaseItems.size} 个愿望清单项")
                            // 同步到本地数据库 / Sync to local database
                            supabaseItems.forEach { supabaseItem ->
                                try {
                                    val localItem = supabaseItem.toWishlistItem()
                                    val entity = WishlistEntity.fromWishlistItem(localItem)
                                    wishlistDao?.insertWishlistItem(entity)
                                    Log.d(TAG, "✅ 已同步到本地: ${supabaseItem.id}")
                                } catch (e: Exception) {
                                    Log.w(TAG, "⚠️ 同步愿望清单项到本地失败: ${supabaseItem.id}", e)
                                }
                            }
                        } else {
                            Log.d(TAG, "ℹ️ Supabase 中没有愿望清单项")
                        }
                    } else {
                        val errorBody = response?.errorBody()?.string()
                        Log.w(TAG, "⚠️ 从 Supabase 获取失败: HTTP ${response?.code()} - $errorBody")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ 从 Supabase 获取愿望清单失败，将使用本地数据", e)
                }
                
                emit(currentUid)
            } catch (e: Exception) {
                Log.e(TAG, "❌ 获取用户ID失败", e)
                emit("dev_user") // 降级到默认用户ID / Fallback to default user ID
            }
        }.flowOn(Dispatchers.IO)
        .flatMapLatest { userId ->
            Log.d(TAG, "🔄 flatMapLatest 开始，用户ID: $userId")
            try {
                wishlistDao.getWishlistByUser(userId).map { entities ->
                    Log.d(TAG, "📦 从本地数据库获取到 ${entities.size} 个实体")
                    val items = entities.map { 
                        try {
                            it.toWishlistItem()
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ 转换愿望清单项失败", e)
                            null
                        }
                    }.filterNotNull()
                    Log.d(TAG, "✅ 愿望清单 Flow 更新，用户ID: $userId, 数量: ${items.size}")
                    if (items.isNotEmpty()) {
                        Log.d(TAG, "📝 愿望清单项列表: ${items.map { it.title }}")
                    }
                    items
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 查询愿望清单失败", e)
                flowOf(emptyList())
            }
        }
    }

    /**
     * 获取当前用户的愿望清单（同步）/ Get current user's wishlist (synchronous)
     */
    suspend fun getWishlistSync(): List<WishlistItem> = withContext(Dispatchers.IO) {
        if (wishlistDao == null) {
            return@withContext emptyList()
        }
        val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
        wishlistDao.getWishlistByUserSync(currentUid).map { it.toWishlistItem() }
    }

    /**
     * 获取所有愿望清单（用于匹配）/ Get all wishlist items (for matching)
     * 优先从 Supabase 获取，失败则从本地获取 / Prioritize Supabase, fallback to local
     */
    suspend fun getAllWishlistItems(): List<WishlistItem> = withContext(Dispatchers.IO) {
        // 优先从 Supabase 获取（用于反向匹配）/ Prioritize Supabase (for reverse matching)
        try {
            val response = supabaseApi?.getAllWishlistItems(100)
            if (response?.isSuccessful == true) {
                val supabaseItems = response.body() ?: emptyList()
                if (supabaseItems.isNotEmpty()) {
                    Log.d(TAG, "从 Supabase 获取到 ${supabaseItems.size} 个愿望清单项")
                    // 同步到本地数据库 / Sync to local database
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
        
        // 从本地数据库获取（降级方案）/ Get from local database (fallback)
        if (wishlistDao == null) {
            return@withContext emptyList()
        }
        val localItems = wishlistDao.getAllWishlistItems().map { it.toWishlistItem() }
        Log.d(TAG, "从本地获取到 ${localItems.size} 个愿望清单项")
        localItems
    }

    /**
     * 智能匹配交换机会 / Intelligent exchange opportunity matching
     * 只匹配"我正在卖的商品"与"别人正在卖的商品" / Only match "items I'm selling" with "items others are selling"
     * 
     * @param minScore 最低匹配分数（默认30）/ Minimum match score (default 30)
     * @param maxResults 最大返回结果数（默认50，0表示不限制）/ Maximum results (default 50, 0 means unlimited)
     */
    suspend fun findExchangeMatches(
        minScore: Double = 30.0,
        maxResults: Int = 50
    ): List<ExchangeMatch> = withContext(Dispatchers.IO) {
        val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
        val currentEmail = authRepo?.getCurrentUserEmail()?.lowercase()
        
        // 获取当前用户发布的商品（我正在卖的商品）/ Get current user's published items (items I'm selling)
        val allItems = itemRepository?.listItems() ?: emptyList()
        val myItems = allItems.filter { item ->
            // 同时匹配 ownerUid 和 ownerEmail / Match both ownerUid and ownerEmail
            val uidMatch = item.ownerUid == currentUid
            val emailMatch = currentEmail?.let { 
                item.ownerEmail.equals(it, ignoreCase = true) 
            } ?: false
            uidMatch || emailMatch
        }

        if (myItems.isEmpty()) {
            Log.d(TAG, "您没有正在出售的商品，无法进行交换匹配")
            return@withContext emptyList()
        }

        // 获取所有其他用户发布的商品（别人正在卖的商品）/ Get all other users' published items (items others are selling)
        val otherItems = allItems.filter { item ->
            val uidNotMatch = item.ownerUid != currentUid
            val emailNotMatch = currentEmail?.let { 
                !item.ownerEmail.equals(it, ignoreCase = true) 
            } ?: true
            uidNotMatch && emailNotMatch
        }

        if (otherItems.isEmpty()) {
            Log.d(TAG, "没有其他用户正在出售的商品")
            return@withContext emptyList()
        }

        val matches = mutableListOf<ExchangeMatch>()

        // 匹配逻辑：我的商品 vs 其他用户的商品 / Matching logic: my items vs other users' items
        for (myItem in myItems) {
            for (otherItem in otherItems) {
                val score = calculateItemMatchScore(myItem, otherItem)
                if (score >= minScore) {
                    val reasons = getItemMatchReasons(myItem, otherItem, score)
                    matches.add(ExchangeMatch(myItem, otherItem, score, reasons))
                }
            }
        }

        // 按匹配分数排序 / Sort by match score
        val sortedMatches = matches.sortedByDescending { it.matchScore }
        
        // 限制返回结果数 / Limit result count
        if (maxResults > 0 && sortedMatches.size > maxResults) {
            return@withContext sortedMatches.take(maxResults)
        }
        
        return@withContext sortedMatches
    }

    /**
     * 匹配单个愿望清单项（已废弃，现在只匹配商品）/ Match single wishlist item (deprecated, now only match items)
     * 保留此方法以保持向后兼容，但返回空列表 / Keep this method for backward compatibility, but returns empty list
     * 
     * @param wishlistItemId 愿望清单项ID / Wishlist item ID
     * @param minScore 最低匹配分数（默认30）/ Minimum match score (default 30)
     * @param maxResults 最大返回结果数（默认20，0表示不限制）/ Maximum results (default 20, 0 means unlimited)
     */
    @Deprecated("现在只匹配商品，不再使用愿望清单匹配")
    suspend fun findMatchesForWishlistItem(
        wishlistItemId: String,
        minScore: Double = 30.0,
        maxResults: Int = 20
    ): List<ExchangeMatch> = withContext(Dispatchers.IO) {
        Log.d(TAG, "findMatchesForWishlistItem已废弃，现在只匹配商品")
        return@withContext emptyList()
    }

    /**
     * 计算两个商品的匹配分数（0-100）/ Calculate match score between two items (0-100)
     * 用于交换匹配：我的商品 vs 其他用户的商品 / For exchange matching: my items vs other users' items
     */
    private fun calculateItemMatchScore(myItem: Item, otherItem: Item): Double {
        var score = 0.0

        // 1. 标题关键词匹配（40分）/ Title keyword matching (40 points)
        val titleMatch = calculateTextSimilarity(myItem.title.lowercase(), otherItem.title.lowercase())
        score += titleMatch * 40

        // 2. 描述匹配（20分）/ Description matching (20 points)
        val descMatch = calculateTextSimilarity(
            myItem.description.lowercase(),
            otherItem.description.lowercase()
        )
        score += descMatch * 20

        // 3. 价格匹配（30分）/ Price matching (30 points)
        val priceScore = calculateItemPriceMatch(myItem, otherItem)
        score += priceScore * 30

        // 4. 类别匹配（10分，如果有类别）/ Category matching (10 points, if category exists)
        if (myItem.category.isNotEmpty() && otherItem.category.isNotEmpty()) {
            val categoryMatch = when {
                myItem.category == otherItem.category -> 1.0 // 完全匹配 / Exact match
                myItem.category.contains(otherItem.category) || 
                otherItem.category.contains(myItem.category) -> 0.7 // 包含匹配 / Contains match
                myItem.title.lowercase().contains(otherItem.category.lowercase()) ||
                otherItem.title.lowercase().contains(myItem.category.lowercase()) -> 0.5 // 文本匹配 / Text match
                else -> 0.0
            }
            score += categoryMatch * 10
        } else {
            score += 10 // 如果都没有类别，给满分 / If no category, give full points
        }

        return score.coerceIn(0.0, 100.0)
    }
    
    /**
     * 计算两个商品的价格匹配分数（0-1）/ Calculate price match score between two items (0-1)
     */
    private fun calculateItemPriceMatch(item1: Item, item2: Item): Double {
        val price1 = item1.price
        val price2 = item2.price
        
        if (price1 <= 0 || price2 <= 0) {
            return 0.5 // 如果价格无效，给中等分数 / If price invalid, give medium score
        }
        
        // 计算价格差异百分比 / Calculate price difference percentage
        val priceDiff = kotlin.math.abs(price1 - price2)
        val avgPrice = (price1 + price2) / 2.0
        val diffRatio = if (avgPrice > 0) priceDiff / avgPrice else 1.0
        
        // 价格越接近，分数越高 / Closer prices get higher scores
        return when {
            diffRatio <= 0.1 -> 1.0  // 价格差异在10%以内，满分 / Price difference within 10%, full score
            diffRatio <= 0.2 -> 0.8  // 价格差异在20%以内 / Price difference within 20%
            diffRatio <= 0.3 -> 0.6  // 价格差异在30%以内 / Price difference within 30%
            diffRatio <= 0.5 -> 0.4  // 价格差异在50%以内 / Price difference within 50%
            else -> 0.2              // 价格差异超过50% / Price difference over 50%
        }
    }

    /**
     * 计算文本相似度（0-1）/ Calculate text similarity (0-1)
     * 使用改进的算法，支持中文分词 / Uses improved algorithm supporting Chinese word segmentation
     */
    private fun calculateTextSimilarity(text1: String, text2: String): Double {
        if (text1.isEmpty() || text2.isEmpty()) return 0.0

        // 支持中英文混合的关键词匹配 / Support mixed Chinese-English keyword matching
        // 中文按字符分割，英文按空格分割 / Chinese split by character, English split by space
        val words1 = extractKeywords(text1)
        val words2 = extractKeywords(text2)

        if (words1.isEmpty() || words2.isEmpty()) return 0.0

        // 计算交集 / Calculate intersection
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

        // 计算并集相似度（Jaccard相似度）/ Calculate union similarity (Jaccard similarity)
        val union = (words1 + words2).distinct().size
        val jaccard = if (union > 0) matches.toDouble() / union else 0.0
        
        // 计算交集与较小集合的比例 / Calculate ratio of intersection to smaller set
        val minSize = minOf(words1.size, words2.size)
        val intersectionRatio = if (minSize > 0) matches.toDouble() / minSize else 0.0
        
        // 综合两种相似度 / Combine both similarities
        return ((jaccard * 0.4 + intersectionRatio * 0.6)).coerceIn(0.0, 1.0)
    }
    
    /**
     * 提取关键词（支持中英文）/ Extract keywords (supports Chinese and English)
     */
    private fun extractKeywords(text: String): List<String> {
        val keywords = mutableListOf<String>()
        
        // 移除标点符号和多余空格 / Remove punctuation and extra spaces
        val cleaned = text.replace(Regex("[，。！？、；：\"\"''（）【】《》\\s]+"), " ")
        
        // 按空格分割（英文单词）/ Split by space (English words)
        val parts = cleaned.split(" ").filter { it.isNotBlank() }
        
        for (part in parts) {
            // 如果是中文（长度>=2），按字符分割 / If Chinese (length>=2), split by character
            if (part.length >= 2 && part.all { it.code >= 0x4E00 && it.code <= 0x9FFF }) {
                // 中文：提取2-4字的词组 / Chinese: extract 2-4 character phrases
                for (i in 0 until part.length - 1) {
                    for (len in 2..minOf(4, part.length - i)) {
                        if (i + len <= part.length) {
                            keywords.add(part.substring(i, i + len))
                        }
                    }
                }
            } else {
                // 英文：过滤掉太短的词 / English: filter out too short words
                if (part.length >= 2) {
                    keywords.add(part.lowercase())
                }
            }
        }
        
        return keywords.distinct()
    }
    
    /**
     * 计算编辑距离相似度（Levenshtein距离）/ Calculate edit distance similarity (Levenshtein distance)
     */
    private fun calculateLevenshteinSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0
        
        val maxLen = maxOf(s1.length, s2.length)
        val distance = levenshteinDistance(s1, s2)
        return 1.0 - (distance.toDouble() / maxLen)
    }
    
    /**
     * 计算Levenshtein距离 / Calculate Levenshtein distance
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
                    dp[i - 1][j] + 1,      // 删除 / Delete
                    dp[i][j - 1] + 1,      // 插入 / Insert
                    dp[i - 1][j - 1] + cost // 替换 / Replace
                )
            }
        }
        
        return dp[m][n]
    }

    /**
     * 计算价格匹配分数（0-1）/ Calculate price match score (0-1)
     */
    private fun calculatePriceMatch(wish: WishlistItem, item: Item): Double {
        val itemPrice = item.price

        // 如果愿望清单没有价格限制，给中等分数 / If wishlist has no price limit, give medium score
        if (wish.minPrice == 0.0 && wish.maxPrice == 0.0) {
            return 0.6
        }

        // 检查价格是否在范围内 / Check if price is within range
        val inRange = (wish.minPrice == 0.0 || itemPrice >= wish.minPrice) &&
                (wish.maxPrice == 0.0 || itemPrice <= wish.maxPrice)

        if (inRange) {
            // 在范围内，计算价格接近度 / Within range, calculate price proximity
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

        // 不在范围内，但价格接近，给部分分数 / Not in range but price close, give partial score
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
     * 获取商品匹配原因 / Get item match reasons
     */
    private fun getItemMatchReasons(myItem: Item, otherItem: Item, score: Double): List<String> {
        val reasons = mutableListOf<String>()

        if (score > 70) {
            reasons.add("高度匹配")
        } else if (score > 50) {
            reasons.add("良好匹配")
        } else {
            reasons.add("可能匹配")
        }

        // 价格匹配 / Price matching
        val priceMatch = calculateItemPriceMatch(myItem, otherItem)
        if (priceMatch > 0.7) {
            reasons.add("价格相近")
        }

        // 标题匹配 / Title matching
        val titleMatch = calculateTextSimilarity(myItem.title.lowercase(), otherItem.title.lowercase())
        if (titleMatch > 0.5) {
            reasons.add("商品类型相似")
        }

        // 类别匹配 / Category matching
        if (myItem.category.isNotEmpty() && otherItem.category.isNotEmpty()) {
            if (otherItem.category == myItem.category) {
                reasons.add("类别完全匹配")
            } else if (otherItem.category.contains(myItem.category) || myItem.category.contains(otherItem.category)) {
                reasons.add("类别相关")
            }
        }

        return reasons
    }
    
    /**
     * 获取匹配原因（保留用于向后兼容，但不再使用）/ Get match reasons (kept for backward compatibility, but no longer used)
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

        // 价格匹配 / Price matching
        val priceMatch = calculatePriceMatch(wish, item)
        if (priceMatch > 0.7) {
            reasons.add("价格合适")
        }

        // 标题匹配 / Title matching
        val titleMatch = calculateTextSimilarity(wish.title.lowercase(), item.title.lowercase())
        if (titleMatch > 0.5) {
            reasons.add("商品类型相似")
        }

        // 类别匹配 / Category matching
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


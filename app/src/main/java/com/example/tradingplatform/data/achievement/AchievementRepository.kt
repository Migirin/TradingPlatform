package com.example.tradingplatform.data.achievement

import android.content.Context
import android.util.Log
import com.example.tradingplatform.data.auth.AuthRepository
import com.example.tradingplatform.data.items.ItemRepository
import com.example.tradingplatform.data.local.AppDatabase
import com.example.tradingplatform.data.local.UserAchievementDao
import com.example.tradingplatform.data.local.UserAchievementEntity
import com.example.tradingplatform.data.wishlist.WishlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class AchievementRepository(
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "AchievementRepository"
    }

    private val database: AppDatabase? = context?.let { AppDatabase.getDatabase(it) }
    private val achievementDao: UserAchievementDao? = database?.userAchievementDao()
    private val authRepo: AuthRepository? = context?.let { AuthRepository(it) }
    private val itemRepository: ItemRepository? = context?.let { ItemRepository(it) }
    private val wishlistRepository: WishlistRepository? = context?.let { WishlistRepository(it) }

    /**
     * 获取用户的成就列表
     */
    fun getUserAchievementsFlow(): Flow<List<UserAchievement>> {
        if (achievementDao == null) {
            return flowOf(emptyList())
        }

        return flow {
            val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
            emit(currentUid)
        }.flatMapLatest { userId ->
            achievementDao.getAchievementsByUser(userId).map { entities ->
                entities.map { it.toUserAchievement() }
            }
        }
    }

    /**
     * 获取用户成就列表（同步）
     */
    suspend fun getUserAchievementsSync(): List<UserAchievement> = withContext(Dispatchers.IO) {
        if (achievementDao == null) {
            return@withContext emptyList()
        }
        val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user"
        achievementDao.getAchievementsByUserSync(currentUid).map { it.toUserAchievement() }
    }

    /**
     * 检查并授予成就
     */
    suspend fun checkAndGrantAchievements() = withContext(Dispatchers.IO) {
        if (achievementDao == null || authRepo == null) {
            return@withContext
        }

        val currentUid = authRepo.getCurrentUserUid() ?: return@withContext
        val currentEmail = authRepo.getCurrentUserEmail() ?: return@withContext

        // 获取用户统计数据
        val stats = getUserStats(currentUid)

        // 检查各种成就
        checkPostAchievements(currentUid, stats.postCount)
        checkMessageAchievements(currentUid, stats.messageCount)
        checkWishlistAchievements(currentUid, stats.wishlistCount)
        checkExchangeAchievements(currentUid, stats.exchangeCount)
        checkPriceAlertAchievements(currentUid, stats.priceAlertCount, stats.priceAlertSuccessCount)
        checkStoryAchievements(currentUid, stats.storyCount)
        checkCategoryAchievements(currentUid, stats.uniqueCategories)
    }

    /**
     * 用户统计数据
     */
    private data class UserStats(
        val postCount: Int = 0,
        val messageCount: Int = 0,
        val wishlistCount: Int = 0,
        val exchangeCount: Int = 0,
        val priceAlertCount: Int = 0,
        val priceAlertSuccessCount: Int = 0,
        val storyCount: Int = 0,
        val uniqueCategories: Set<String> = emptySet()
    )

    /**
     * 获取用户统计数据
     */
    private suspend fun getUserStats(userId: String): UserStats {
        val items = itemRepository?.listItems()?.filter { it.ownerUid == userId } ?: emptyList()
        val wishlist = wishlistRepository?.getWishlistSync() ?: emptyList()
        
        // 统计消息数量（从数据库获取）
        val messageCount = if (database != null) {
            try {
                val chatDao = database.chatMessageDao()
                chatDao.getMessageCountForUser(userId)
            } catch (e: Exception) {
                Log.w(TAG, "获取消息统计失败", e)
                0
            }
        } else {
            0
        }
        
        return UserStats(
            postCount = items.size,
            messageCount = messageCount,
            wishlistCount = wishlist.size,
            exchangeCount = 0, // TODO: 跟踪交换次数（需要添加交换记录表）
            priceAlertCount = wishlist.count { it.enablePriceAlert },
            priceAlertSuccessCount = 0, // TODO: 跟踪成功触发的提醒
            storyCount = items.count { it.story.isNotEmpty() },
            uniqueCategories = items.map { it.category }.filter { it.isNotEmpty() }.toSet()
        )
    }

    /**
     * 检查发布商品相关成就
     */
    private suspend fun checkPostAchievements(userId: String, postCount: Int) {
        val achievements = listOf(
            AchievementType.FIRST_POST to (postCount >= 1),
            AchievementType.POST_5 to (postCount >= 5),
            AchievementType.POST_10 to (postCount >= 10),
            AchievementType.POST_20 to (postCount >= 20)
        )

        achievements.forEach { (type, unlocked) ->
            if (unlocked) {
                grantAchievement(userId, type, postCount, getTargetForType(type))
            }
        }
    }

    /**
     * 检查消息相关成就
     */
    private suspend fun checkMessageAchievements(userId: String, messageCount: Int) {
        val achievements = listOf(
            AchievementType.FIRST_MESSAGE to (messageCount >= 1),
            AchievementType.MESSAGE_10 to (messageCount >= 10),
            AchievementType.MESSAGE_50 to (messageCount >= 50)
        )

        achievements.forEach { (type, unlocked) ->
            if (unlocked) {
                grantAchievement(userId, type, messageCount, getTargetForType(type))
            }
        }
    }

    /**
     * 检查愿望清单相关成就
     */
    private suspend fun checkWishlistAchievements(userId: String, wishlistCount: Int) {
        val achievements = listOf(
            AchievementType.FIRST_WISHLIST to (wishlistCount >= 1),
            AchievementType.WISHLIST_5 to (wishlistCount >= 5),
            AchievementType.WISHLIST_10 to (wishlistCount >= 10)
        )

        achievements.forEach { (type, unlocked) ->
            if (unlocked) {
                grantAchievement(userId, type, wishlistCount, getTargetForType(type))
            }
        }
    }

    /**
     * 检查交换相关成就
     */
    private suspend fun checkExchangeAchievements(userId: String, exchangeCount: Int) {
        val achievements = listOf(
            AchievementType.FIRST_EXCHANGE to (exchangeCount >= 1),
            AchievementType.EXCHANGE_5 to (exchangeCount >= 5),
            AchievementType.EXCHANGE_10 to (exchangeCount >= 10)
        )

        achievements.forEach { (type, unlocked) ->
            if (unlocked) {
                grantAchievement(userId, type, exchangeCount, getTargetForType(type))
            }
        }
    }

    /**
     * 检查价格提醒相关成就
     */
    private suspend fun checkPriceAlertAchievements(
        userId: String,
        priceAlertCount: Int,
        priceAlertSuccessCount: Int
    ) {
        if (priceAlertCount >= 1) {
            grantAchievement(userId, AchievementType.PRICE_ALERT, priceAlertCount, 1)
        }
        if (priceAlertSuccessCount >= 1) {
            grantAchievement(userId, AchievementType.PRICE_ALERT_SUCCESS, priceAlertSuccessCount, 1)
        }
    }

    /**
     * 检查故事相关成就
     */
    private suspend fun checkStoryAchievements(userId: String, storyCount: Int) {
        if (storyCount >= 1) {
            grantAchievement(userId, AchievementType.STORY_TELLER, storyCount, 1)
        }
        if (storyCount >= 5) {
            grantAchievement(userId, AchievementType.STORY_5, storyCount, 5)
        }
    }

    /**
     * 检查类别相关成就
     */
    private suspend fun checkCategoryAchievements(userId: String, uniqueCategories: Set<String>) {
        // 检查是否使用了所有类别（简化版，检查是否使用了至少5个不同类别）
        if (uniqueCategories.size >= 5) {
            grantAchievement(userId, AchievementType.CATEGORY_EXPERT, uniqueCategories.size, 5)
        }
    }

    /**
     * 授予成就
     */
    private suspend fun grantAchievement(
        userId: String,
        type: AchievementType,
        progress: Int,
        target: Int
    ) {
        if (achievementDao == null) return

        // 检查是否已经解锁
        val existing = achievementDao.getAchievementByType(userId, type.id)
        if (existing != null && existing.progress >= target) {
            return // 已经解锁
        }

        val achievement = UserAchievement(
            id = existing?.id ?: UUID.randomUUID().toString(),
            userId = userId,
            achievementType = type,
            progress = progress,
            target = target,
            unlockedAt = if (existing == null) System.currentTimeMillis() else existing.unlockedAt
        )

        val entity = UserAchievementEntity.fromUserAchievement(achievement)
        achievementDao.insertAchievement(entity)
        
        // 如果是新解锁的成就，发送通知
        if (progress >= target && (existing == null || existing.progress < target)) {
            Log.d(TAG, "成就解锁: ${type.displayName} - ${type.description}")
            if (context != null) {
                val notificationService = AchievementNotificationService(context)
                notificationService.notifyAchievementUnlocked(achievement)
            }
        }
    }

    /**
     * 获取成就的目标值
     */
    private fun getTargetForType(type: AchievementType): Int {
        return when (type) {
            AchievementType.FIRST_POST, AchievementType.FIRST_MESSAGE,
            AchievementType.FIRST_WISHLIST, AchievementType.FIRST_EXCHANGE,
            AchievementType.PRICE_ALERT, AchievementType.PRICE_ALERT_SUCCESS,
            AchievementType.STORY_TELLER, AchievementType.CATEGORY_EXPERT,
            AchievementType.EARLY_BIRD, AchievementType.LOYAL_USER -> 1
            AchievementType.POST_5, AchievementType.WISHLIST_5, AchievementType.EXCHANGE_5 -> 5
            AchievementType.POST_10, AchievementType.MESSAGE_10,
            AchievementType.WISHLIST_10, AchievementType.EXCHANGE_10 -> 10
            AchievementType.POST_20 -> 20
            AchievementType.MESSAGE_50 -> 50
            AchievementType.STORY_5 -> 5
        }
    }
}


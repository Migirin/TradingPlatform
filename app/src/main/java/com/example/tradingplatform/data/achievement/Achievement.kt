package com.example.tradingplatform.data.achievement

/**
 * 成就定义
 */
enum class AchievementType(
    val id: String,
    val displayName: String, // 显示名称（避免与Enum.name冲突）
    val description: String,
    val icon: String // 使用emoji作为图标
) {
    FIRST_POST("first_post", "首次发布", "发布第一个商品", "🎯"),
    POST_5("post_5", "小卖家", "发布5个商品", "📦"),
    POST_10("post_10", "活跃卖家", "发布10个商品", "🏪"),
    POST_20("post_20", "资深卖家", "发布20个商品", "🏬"),
    
    FIRST_MESSAGE("first_message", "初次交流", "发送第一条消息", "💬"),
    MESSAGE_10("message_10", "社交达人", "发送10条消息", "📱"),
    MESSAGE_50("message_50", "沟通专家", "发送50条消息", "📞"),
    
    FIRST_WISHLIST("first_wishlist", "愿望清单", "添加第一个愿望清单", "⭐"),
    WISHLIST_5("wishlist_5", "愿望收集者", "添加5个愿望清单", "✨"),
    WISHLIST_10("wishlist_10", "梦想家", "添加10个愿望清单", "🌟"),
    
    FIRST_EXCHANGE("first_exchange", "首次交换", "完成第一次交换匹配", "🔄"),
    EXCHANGE_5("exchange_5", "交换达人", "完成5次交换匹配", "🔄🔄"),
    EXCHANGE_10("exchange_10", "交换大师", "完成10次交换匹配", "🔄🔄🔄"),
    
    PRICE_ALERT("price_alert", "价格猎人", "设置第一个降价提醒", "💰"),
    PRICE_ALERT_SUCCESS("price_alert_success", "捡漏王", "降价提醒成功触发", "💎"),
    
    STORY_TELLER("story_teller", "故事讲述者", "为商品添加故事", "📖"),
    STORY_5("story_5", "情感卖家", "为5个商品添加故事", "📚"),
    
    CATEGORY_EXPERT("category_expert", "分类专家", "使用所有商品类别", "🏷️"),
    
    EARLY_BIRD("early_bird", "早起鸟", "在应用发布后7天内注册", "🐦"),
    
    LOYAL_USER("loyal_user", "忠实用户", "连续使用30天", "👑")
}

/**
 * 用户成就记录
 */
data class UserAchievement(
    val id: String = "",
    val userId: String = "",
    val achievementType: AchievementType,
    val unlockedAt: Long = System.currentTimeMillis(),
    val progress: Int = 0, // 当前进度（用于需要多步完成的成就）
    val target: Int = 1 // 目标值
) {
    val isUnlocked: Boolean
        get() = progress >= target
}


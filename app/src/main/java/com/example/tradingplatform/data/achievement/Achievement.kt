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

fun AchievementType.getTitle(isEnglish: Boolean): String {
    return when (this) {
        AchievementType.FIRST_POST -> if (isEnglish) "First listing" else "首次发布"
        AchievementType.POST_5 -> if (isEnglish) "Junior seller" else "小卖家"
        AchievementType.POST_10 -> if (isEnglish) "Active seller" else "活跃卖家"
        AchievementType.POST_20 -> if (isEnglish) "Senior seller" else "资深卖家"

        AchievementType.FIRST_MESSAGE -> if (isEnglish) "First chat" else "初次交流"
        AchievementType.MESSAGE_10 -> if (isEnglish) "Social butterfly" else "社交达人"
        AchievementType.MESSAGE_50 -> if (isEnglish) "Communication expert" else "沟通专家"

        AchievementType.FIRST_WISHLIST -> if (isEnglish) "First wishlist" else "愿望清单"
        AchievementType.WISHLIST_5 -> if (isEnglish) "Wishlist collector" else "愿望收集者"
        AchievementType.WISHLIST_10 -> if (isEnglish) "Dreamer" else "梦想家"

        AchievementType.FIRST_EXCHANGE -> if (isEnglish) "First exchange" else "首次交换"
        AchievementType.EXCHANGE_5 -> if (isEnglish) "Exchange enthusiast" else "交换达人"
        AchievementType.EXCHANGE_10 -> if (isEnglish) "Exchange master" else "交换大师"

        AchievementType.PRICE_ALERT -> if (isEnglish) "Price hunter" else "价格猎人"
        AchievementType.PRICE_ALERT_SUCCESS -> if (isEnglish) "Bargain master" else "捡漏王"

        AchievementType.STORY_TELLER -> if (isEnglish) "Storyteller" else "故事讲述者"
        AchievementType.STORY_5 -> if (isEnglish) "Emotional seller" else "情感卖家"

        AchievementType.CATEGORY_EXPERT -> if (isEnglish) "Category expert" else "分类专家"
        AchievementType.EARLY_BIRD -> if (isEnglish) "Early bird" else "早起鸟"
        AchievementType.LOYAL_USER -> if (isEnglish) "Loyal user" else "忠实用户"
    }
}

fun AchievementType.getDescription(isEnglish: Boolean): String {
    return when (this) {
        AchievementType.FIRST_POST -> if (isEnglish) "Post your first item" else "发布第一个商品"
        AchievementType.POST_5 -> if (isEnglish) "Post 5 items" else "发布5个商品"
        AchievementType.POST_10 -> if (isEnglish) "Post 10 items" else "发布10个商品"
        AchievementType.POST_20 -> if (isEnglish) "Post 20 items" else "发布20个商品"

        AchievementType.FIRST_MESSAGE -> if (isEnglish) "Send your first message" else "发送第一条消息"
        AchievementType.MESSAGE_10 -> if (isEnglish) "Send 10 messages" else "发送10条消息"
        AchievementType.MESSAGE_50 -> if (isEnglish) "Send 50 messages" else "发送50条消息"

        AchievementType.FIRST_WISHLIST -> if (isEnglish) "Add your first wishlist item" else "添加第一个愿望清单"
        AchievementType.WISHLIST_5 -> if (isEnglish) "Add 5 wishlist items" else "添加5个愿望清单"
        AchievementType.WISHLIST_10 -> if (isEnglish) "Add 10 wishlist items" else "添加10个愿望清单"

        AchievementType.FIRST_EXCHANGE -> if (isEnglish) "Complete your first exchange match" else "完成第一次交换匹配"
        AchievementType.EXCHANGE_5 -> if (isEnglish) "Complete 5 exchange matches" else "完成5次交换匹配"
        AchievementType.EXCHANGE_10 -> if (isEnglish) "Complete 10 exchange matches" else "完成10次交换匹配"

        AchievementType.PRICE_ALERT -> if (isEnglish) "Set your first price alert" else "设置第一个降价提醒"
        AchievementType.PRICE_ALERT_SUCCESS -> if (isEnglish) "Have a price alert triggered successfully" else "降价提醒成功触发"

        AchievementType.STORY_TELLER -> if (isEnglish) "Add a story to an item" else "为商品添加故事"
        AchievementType.STORY_5 -> if (isEnglish) "Add stories to 5 items" else "为5个商品添加故事"

        AchievementType.CATEGORY_EXPERT -> if (isEnglish) "Use all item categories" else "使用所有商品类别"
        AchievementType.EARLY_BIRD -> if (isEnglish) "Register within 7 days after the app launch" else "在应用发布后7天内注册"
        AchievementType.LOYAL_USER -> if (isEnglish) "Use the app for 30 consecutive days" else "连续使用30天"
    }
}

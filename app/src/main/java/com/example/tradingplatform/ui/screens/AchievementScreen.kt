package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradingplatform.data.achievement.AchievementType
import com.example.tradingplatform.data.achievement.UserAchievement
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppStrings
import com.example.tradingplatform.ui.viewmodel.AchievementViewModel

@Composable
fun AchievementScreen(
    onBack: () -> Unit,
    viewModel: AchievementViewModel = viewModel()
) {
    val achievements by viewModel.achievements.collectAsState()
    val unlockedCount = viewModel.getUnlockedCount()
    val totalCount = viewModel.getTotalCount()

    // 获取所有成就类型
    val allAchievementTypes = AchievementType.values()
    
    // 创建成就映射（已解锁的 + 未解锁的）
    val achievementMap = achievements.associateBy { it.achievementType.id }
    val allAchievements = allAchievementTypes.map { type ->
        achievementMap[type.id] ?: UserAchievement(
            achievementType = type,
            progress = 0,
            target = getTargetForType(type)
        )
    }

    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 顶部标题和统计
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.achievementsTitle,
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onBack) {
                Text(strings.myBack)
            }
        }

        // 成就统计卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$unlockedCount / $totalCount",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = strings.achievementsUnlockedCountLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                // 进度条
                val progress = unlockedCount.toFloat() / totalCount.toFloat()
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // 成就列表
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(allAchievements, key = { it.achievementType.id }) { achievement ->
                AchievementCard(achievement = achievement)
            }
        }
    }
}

private fun getAchievementTitle(type: AchievementType, isEnglish: Boolean): String {
    return when (type) {
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

private fun getAchievementDescription(type: AchievementType, isEnglish: Boolean): String {
    return when (type) {
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

@Composable
fun AchievementCard(achievement: UserAchievement) {
    val isUnlocked = achievement.isUnlocked
    val progress = achievement.progress.toFloat()
    val target = achievement.target.toFloat()
    val progressPercent = (progress / target).coerceIn(0f, 1f)
    val lang = LocalAppLanguage.current
    val isEnglish = lang == AppLanguage.EN
    val title = getAchievementTitle(achievement.achievementType, isEnglish)
    val description = getAchievementDescription(achievement.achievementType, isEnglish)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 成就图标
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = if (isUnlocked) {
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.achievementType.icon,
                    fontSize = 32.sp
                )
            }

            // 成就信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 进度条（对于需要多步完成的成就）
                if (achievement.target > 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${achievement.progress} / ${achievement.target}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isUnlocked) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = LocalAppStrings.current.achievementsUnlockedBadge,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                        LinearProgressIndicator(
                            progress = progressPercent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (isUnlocked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                } else {
                    // 单步成就
                    if (isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = LocalAppStrings.current.achievementsUnlockedBadge,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Text(
                            text = LocalAppStrings.current.achievementsLockedBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

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


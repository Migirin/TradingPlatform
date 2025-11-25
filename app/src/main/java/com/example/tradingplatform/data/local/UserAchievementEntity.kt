package com.example.tradingplatform.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tradingplatform.data.achievement.AchievementType

/**
 * 用户成就实体类（Room）
 */
@Entity(tableName = "user_achievements")
data class UserAchievementEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "achievement_type")
    val achievementType: String, // 存储 AchievementType.id
    @ColumnInfo(name = "unlocked_at")
    val unlockedAt: Long,
    val progress: Int,
    val target: Int
) {
    fun toUserAchievement(): com.example.tradingplatform.data.achievement.UserAchievement {
        val type = AchievementType.values().find { it.id == achievementType }
            ?: AchievementType.FIRST_POST // 默认值
        
        return com.example.tradingplatform.data.achievement.UserAchievement(
            id = id,
            userId = userId,
            achievementType = type,
            unlockedAt = unlockedAt,
            progress = progress,
            target = target
        )
    }

    companion object {
        fun fromUserAchievement(achievement: com.example.tradingplatform.data.achievement.UserAchievement): UserAchievementEntity {
            return UserAchievementEntity(
                id = achievement.id.ifEmpty { "ach_${System.currentTimeMillis()}_${achievement.achievementType.id}" },
                userId = achievement.userId,
                achievementType = achievement.achievementType.id,
                unlockedAt = achievement.unlockedAt,
                progress = achievement.progress,
                target = achievement.target
            )
        }
    }
}



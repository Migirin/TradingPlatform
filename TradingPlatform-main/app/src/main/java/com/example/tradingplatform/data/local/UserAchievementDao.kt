package com.example.tradingplatform.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 用户成就数据访问对象
 */
@Dao
interface UserAchievementDao {
    @Query("SELECT * FROM user_achievements WHERE user_id = :userId ORDER BY unlocked_at DESC")
    fun getAchievementsByUser(userId: String): Flow<List<UserAchievementEntity>>

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId ORDER BY unlocked_at DESC")
    suspend fun getAchievementsByUserSync(userId: String): List<UserAchievementEntity>

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId AND achievement_type = :achievementType LIMIT 1")
    suspend fun getAchievementByType(userId: String, achievementType: String): UserAchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: UserAchievementEntity)

    @Query("SELECT COUNT(*) FROM user_achievements WHERE user_id = :userId")
    suspend fun getAchievementCount(userId: String): Int
}




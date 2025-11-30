package com.example.tradingplatform.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 应用数据库
 */
@Database(
    entities = [
        User::class,
        ItemEntity::class,
        ChatMessageEntity::class,
        WishlistEntity::class,
        UserAchievementEntity::class,
        TimetableCourseEntity::class
    ],
    version = 9, // 版本升级：添加课表课程表
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun itemDao(): ItemDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun userAchievementDao(): UserAchievementDao
    abstract fun timetableCourseDao(): TimetableCourseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trading_platform_database"
                )
                    .fallbackToDestructiveMigration() // 开发期：数据库版本变更时重建
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


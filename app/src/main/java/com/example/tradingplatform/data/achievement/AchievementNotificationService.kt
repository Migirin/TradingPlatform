package com.example.tradingplatform.data.achievement

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tradingplatform.MainActivity
import com.example.tradingplatform.data.auth.AuthRepository

/**
 * 成就解锁通知服务
 */
class AchievementNotificationService(
    private val context: Context
) {
    companion object {
        private const val TAG = "AchievementNotification"
        private const val CHANNEL_ID = "achievement_channel"
        private const val CHANNEL_NAME = "成就通知 / Achievements"
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "成就解锁通知 / Achievement unlock notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 发送成就解锁通知
     */
    fun notifyAchievementUnlocked(achievement: UserAchievement) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigateTo", "achievements")
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                achievement.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val authRepo = AuthRepository(context)
            val preferredLang = authRepo.getPreferredLanguage()
            val isEnglish = preferredLang == "EN"

            val titleText = if (isEnglish) {
                "🎉 Achievement unlocked!"
            } else {
                "🎉 成就解锁！"
            }

            val achievementTitle = achievement.achievementType.getTitle(isEnglish)
            val achievementDesc = achievement.achievementType.getDescription(isEnglish)

            val contentText = "${achievement.achievementType.icon} $achievementTitle"

            val bigText = if (isEnglish) {
                "Congratulations, you have unlocked: $achievementTitle\n$achievementDesc"
            } else {
                "恭喜您解锁成就：$achievementTitle\n$achievementDesc"
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titleText)
                .setContentText(contentText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(bigText)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(achievement.id.hashCode(), notification)
            Log.d(TAG, "成就解锁通知已发送: ${achievement.achievementType.displayName}")
        } catch (e: Exception) {
            Log.e(TAG, "发送成就解锁通知失败", e)
        }
    }
}


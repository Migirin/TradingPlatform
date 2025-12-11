package com.example.tradingplatform.data.wishlist

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
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.data.items.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 价格提醒服务
 */
class PriceAlertService(
    private val context: Context
) {
    companion object {
        private const val TAG = "PriceAlertService"
        private const val CHANNEL_ID = "price_alert_channel"
        private const val CHANNEL_NAME = "价格提醒 / Price alert"
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
                description = "商品降价提醒通知 / Price drop alerts"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 检查价格变化并发送提醒
     */
    suspend fun checkPriceAlerts(wishlistItems: List<WishlistItem>) = withContext(Dispatchers.IO) {
        val itemRepository = ItemRepository(context)
        
        for (wish in wishlistItems) {
            if (!wish.enablePriceAlert || wish.targetPrice <= 0) {
                continue
            }

            // 如果有关联的商品ID，检查该商品的价格
            if (wish.itemId.isNotEmpty()) {
                val item = itemRepository.getItemById(wish.itemId)
                if (item != null && item.price <= wish.targetPrice) {
                    sendPriceAlert(wish, item)
                }
            } else {
                // 如果没有关联商品ID，查找匹配的商品
                val allItems = itemRepository.listItems()
                val matchingItems = allItems.filter { item ->
                    // 检查商品是否匹配愿望清单的条件
                    val titleMatch = item.title.contains(wish.title, ignoreCase = true) ||
                            wish.title.contains(item.title, ignoreCase = true)
                    val categoryMatch = wish.category.isEmpty() || item.category == wish.category
                    val priceMatch = item.price <= wish.targetPrice &&
                            (wish.minPrice == 0.0 || item.price >= wish.minPrice) &&
                            (wish.maxPrice == 0.0 || item.price <= wish.maxPrice)
                    
                    titleMatch && categoryMatch && priceMatch
                }

                if (matchingItems.isNotEmpty()) {
                    // 找到匹配的商品，发送提醒
                    matchingItems.forEach { item ->
                        sendPriceAlert(wish, item)
                    }
                }
            }
        }
    }

    /**
     * 发送价格提醒通知（公开方法，供外部调用）/ Send price alert notification (public method for external use)
     */
    fun sendPriceAlertForItem(wish: WishlistItem, item: Item) {
        sendPriceAlert(wish, item)
    }

    /**
     * 发送价格提醒通知
     */
    private fun sendPriceAlert(wish: WishlistItem, item: Item) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("itemId", item.id)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                wish.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val authRepo = AuthRepository(context)
            val preferredLang = authRepo.getPreferredLanguage()
            val isEnglish = preferredLang == "EN"

            val currentPriceText = String.format("%.2f", item.price)
            val targetPriceText = String.format("%.2f", wish.targetPrice)

            val title = if (isEnglish) {
                "Price alert"
            } else {
                "商品降价提醒"
            }

            val contentText = if (isEnglish) {
                "${item.title} has dropped to ¥$currentPriceText"
            } else {
                "${item.title} 已降至 ¥$currentPriceText"
            }

            val bigText = if (isEnglish) {
                "The item \"${item.title}\" has dropped to ¥$currentPriceText, which is below your target price ¥$targetPriceText."
            } else {
                "您关注的商品「${item.title}」价格已降至 ¥$currentPriceText，低于您的目标价格 ¥$targetPriceText"
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(bigText)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(wish.id.hashCode(), notification)
            Log.d(TAG, "价格提醒已发送: ${item.title} - ¥${item.price}")
        } catch (e: Exception) {
            Log.e(TAG, "发送价格提醒失败", e)
        }
    }
}


package com.example.tradingplatform.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 愿望清单实体类（Room）
 */
@Entity(tableName = "wishlist")
data class WishlistEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "user_email")
    val userEmail: String,
    val title: String,
    val category: String,
    @ColumnInfo(name = "min_price")
    val minPrice: Double,
    @ColumnInfo(name = "max_price")
    val maxPrice: Double,
    @ColumnInfo(name = "target_price")
    val targetPrice: Double,
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "enable_price_alert")
    val enablePriceAlert: Boolean,
    val description: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
) {
    fun toWishlistItem(): com.example.tradingplatform.data.wishlist.WishlistItem {
        return com.example.tradingplatform.data.wishlist.WishlistItem(
            id = id,
            userId = userId,
            userEmail = userEmail,
            title = title,
            category = category,
            minPrice = minPrice,
            maxPrice = maxPrice,
            targetPrice = targetPrice,
            itemId = itemId,
            enablePriceAlert = enablePriceAlert,
            description = description,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt)
        )
    }

    companion object {
        fun fromWishlistItem(item: com.example.tradingplatform.data.wishlist.WishlistItem): WishlistEntity {
            return WishlistEntity(
                id = item.id.ifEmpty { "wish_${System.currentTimeMillis()}" },
                userId = item.userId,
                userEmail = item.userEmail,
                title = item.title,
                category = item.category,
                minPrice = item.minPrice,
                maxPrice = item.maxPrice,
                targetPrice = item.targetPrice,
                itemId = item.itemId,
                enablePriceAlert = item.enablePriceAlert,
                description = item.description,
                createdAt = item.createdAt.time,
                updatedAt = item.updatedAt.time
            )
        }
    }
}


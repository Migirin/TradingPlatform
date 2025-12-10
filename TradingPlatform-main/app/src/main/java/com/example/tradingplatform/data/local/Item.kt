package com.example.tradingplatform.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 商品实体类（Room）
 */
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val price: Double,
    val description: String,
    val category: String, // 商品类别
    val story: String, // 商品故事/背景
    val imageUrl: String, // 本地文件路径或 URI
    val phoneNumber: String,
    val ownerUid: String,
    val ownerEmail: String,
    val createdAt: Long, // 使用 Long 存储时间戳
    val updatedAt: Long
) {
    // 转换为业务层 Item
    fun toItem(): com.example.tradingplatform.data.items.Item {
        return com.example.tradingplatform.data.items.Item(
            id = id,
            title = title,
            price = price,
            description = description,
            category = category,
            story = story,
            imageUrl = imageUrl,
            phoneNumber = phoneNumber,
            ownerUid = ownerUid,
            ownerEmail = ownerEmail,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt)
        )
    }

    companion object {
        fun fromItem(item: com.example.tradingplatform.data.items.Item): ItemEntity {
            return ItemEntity(
                id = item.id.ifEmpty { "item_${System.currentTimeMillis()}" },
                title = item.title,
                price = item.price,
                description = item.description,
                category = item.category,
                story = item.story,
                imageUrl = item.imageUrl,
                phoneNumber = item.phoneNumber,
                ownerUid = item.ownerUid,
                ownerEmail = item.ownerEmail,
                createdAt = item.createdAt.time,
                updatedAt = item.updatedAt.time
            )
        }
    }
}




package com.example.tradingplatform.data.wishlist

import java.util.Date
import java.util.UUID

/**
 * 愿望清单项（用户想买的商品）
 */
data class WishlistItem(
    val id: String = "",
    val userId: String = "", // 用户ID
    val userEmail: String = "", // 用户邮箱
    val title: String = "", // 商品名称/描述
    val category: String = "", // 商品类别（可选）
    val minPrice: Double = 0.0, // 最低价格（可选，0表示不限制）
    val maxPrice: Double = 0.0, // 最高价格（可选，0表示不限制）
    val targetPrice: Double = 0.0, // 目标价格（降价提醒，0表示不启用）
    val itemId: String = "", // 关联的商品ID（如果是从商品详情页添加的）
    val enablePriceAlert: Boolean = false, // 是否启用降价提醒
    val description: String = "", // 详细描述
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * 交换匹配结果
 */
data class ExchangeMatch(
    val wishlistItem: WishlistItem, // 用户的愿望清单项
    val availableItem: com.example.tradingplatform.data.items.Item, // 可交换的商品
    val matchScore: Double, // 匹配分数（0-100）
    val matchReasons: List<String> // 匹配原因
)



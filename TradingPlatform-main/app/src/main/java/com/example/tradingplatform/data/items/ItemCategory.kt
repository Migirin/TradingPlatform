package com.example.tradingplatform.data.items

/**
 * 商品类别
 */
object ItemCategory {
    val categories = listOf(
        "电子产品",
        "服装配饰",
        "图书文具",
        "家具家电",
        "运动健身",
        "美妆护肤",
        "食品饮料",
        "玩具模型",
        "汽车用品",
        "其他"
    )

    fun isValid(category: String): Boolean {
        return categories.contains(category)
    }
}





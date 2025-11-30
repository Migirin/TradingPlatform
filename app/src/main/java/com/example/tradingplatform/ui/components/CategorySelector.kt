package com.example.tradingplatform.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tradingplatform.data.items.ItemCategory
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppLanguage

@Composable
fun CategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val isEnglish = lang == AppLanguage.EN

    Column(modifier = modifier) {
        Text(
            text = if (isEnglish) "Item category (optional)" else "商品类别（可选）",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ItemCategory.categories) { category ->
                CategoryChip(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val isEnglish = lang == AppLanguage.EN
    val label = when (category) {
        "电子产品" -> if (isEnglish) "Electronics" else "电子产品"
        "服装配饰" -> if (isEnglish) "Clothing & accessories" else "服装配饰"
        "图书文具" -> if (isEnglish) "Books & stationery" else "图书文具"
        "家具家电" -> if (isEnglish) "Furniture & appliances" else "家具家电"
        "运动健身" -> if (isEnglish) "Sports & fitness" else "运动健身"
        "美妆护肤" -> if (isEnglish) "Beauty & skincare" else "美妆护肤"
        "食品饮料" -> if (isEnglish) "Food & beverages" else "食品饮料"
        "玩具模型" -> if (isEnglish) "Toys & models" else "玩具模型"
        "汽车用品" -> if (isEnglish) "Car accessories" else "汽车用品"
        "其他" -> if (isEnglish) "Other" else "其他"
        else -> category
    }
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}





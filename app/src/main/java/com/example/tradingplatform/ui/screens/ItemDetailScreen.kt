package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppLanguage
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ItemDetailScreen(
    item: Item,
    onBack: () -> Unit,
    onContact: () -> Unit,
    onExchangeMatch: () -> Unit = {},
    onAddToWishlist: () -> Unit = {},
    onDelete: (() -> Unit)? = null,
    isDevMode: Boolean = false,
    isOwnItem: Boolean = false
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val lang = LocalAppLanguage.current
    val isEnglish = lang == AppLanguage.EN

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部返回按钮 / Top back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEnglish) "Item details" else "商品详情",
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = onBack) {
                Text(if (isEnglish) "Back" else "返回")
            }
        }

        // 商品图片 / Item image
        if (item.imageUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(item.imageUrl),
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isEnglish) "No image" else "无图片",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 商品信息 / Item information
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(16.dp)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题和价格 / Title and price
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                if (item.category.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = item.category,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = "¥${String.format("%.2f", item.price)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 描述 / Description
            if (item.description.isNotEmpty()) {
                Column {
                    Text(
                        text = if (isEnglish) "Description" else "商品描述",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 商品故事/背景墙（特殊样式展示）/ Item story/background wall (special style display)
            if (item.story.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 标题和图标 / Title and icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = if (isEnglish) "Item story" else "商品故事",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = if (isEnglish) "Item story" else "商品故事",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // 故事内容 / Story content
                            Text(
                                text = item.story,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
                            )
                        }
                    }
                }
            }

            // 商品信息卡片 / Item information card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEnglish) "Seller" else "发布者",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.ownerEmail,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEnglish) "Phone" else "联系电话",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.phoneNumber.ifEmpty { if (isEnglish) "Not provided" else "未提供" },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEnglish) "Posted at" else "发布时间",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormat.format(item.createdAt),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 操作按钮 / Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onContact,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isEnglish) "Contact seller" else "联系卖家")
                    }
                }
                // 只有不是自己的商品才能加入愿望清单 / Only items that are not own can be added to wishlist
                if (!isOwnItem) {
                    OutlinedButton(
                        onClick = onAddToWishlist,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isEnglish) "Add to wishlist" else "加入愿望清单")
                    }
                }
                // 开发者模式显示删除按钮 / Developer mode shows delete button
                if (isDevMode && onDelete != null) {
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(if (isEnglish) "Delete" else "删除")
                    }
                }
            }
        }
    }
}


package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradingplatform.data.wishlist.WishlistItem
import com.example.tradingplatform.ui.viewmodel.WishlistViewModel

@Composable
fun WishlistScreen(
    onBack: () -> Unit,
    onFindMatches: () -> Unit,
    onFindMatchesForItem: (String) -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    viewModel: WishlistViewModel = viewModel()
) {
    val wishlist by viewModel.wishlist.collectAsState()
    val uiState by viewModel.state.collectAsState()
    
    // 调试日志
    LaunchedEffect(wishlist.size) {
        android.util.Log.d("WishlistScreen", "愿望清单数量: ${wishlist.size}")
    }
    
    // 显示错误状态
    if (uiState is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Error) {
        val errorMessage = (uiState as com.example.tradingplatform.ui.viewmodel.WishlistUiState.Error).message
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = "错误: $errorMessage",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 顶部标题和按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我的愿望清单",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onBack) {
                Text("返回")
            }
        }

        // 操作按钮
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onFindMatches,
                    modifier = Modifier.weight(1f),
                    enabled = wishlist.isNotEmpty()
                ) {
                    Text("查找匹配")
                }
            }
            OutlinedButton(
                onClick = onAchievementsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("我的成就")
            }
        }

        // 愿望清单列表
        if (wishlist.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "愿望清单为空",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "在商品详情页点击「加入愿望清单」添加你想买的商品",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(wishlist, key = { it.id }) { item ->
                    WishlistItemCard(
                        item = item,
                        onDelete = { viewModel.deleteWishlistItem(item.id) },
                        onFindMatches = { onFindMatchesForItem(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun WishlistItemCard(
    item: WishlistItem,
    onDelete: () -> Unit,
    onFindMatches: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (item.category.isNotEmpty()) {
                Text(
                    text = "类别: ${item.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.minPrice > 0 || item.maxPrice > 0) {
                val priceRange = when {
                    item.minPrice > 0 && item.maxPrice > 0 -> "¥${item.minPrice} - ¥${item.maxPrice}"
                    item.minPrice > 0 -> "¥${item.minPrice} 以上"
                    else -> "¥${item.maxPrice} 以下"
                }
                Text(
                    text = "价格: $priceRange",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.enablePriceAlert && item.targetPrice > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "降价提醒: 目标价格 ¥${String.format("%.2f", item.targetPrice)}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (item.description.isNotEmpty()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 匹配按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onFindMatches,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("匹配此商品")
                }
            }
        }
    }
}


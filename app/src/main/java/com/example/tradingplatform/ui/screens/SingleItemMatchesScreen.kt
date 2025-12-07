package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.tradingplatform.data.wishlist.ExchangeMatch
import com.example.tradingplatform.ui.viewmodel.WishlistViewModel

@Composable
fun SingleItemMatchesScreen(
    wishlistItemId: String,
    onBack: () -> Unit,
    onItemClick: (com.example.tradingplatform.data.items.Item) -> Unit,
    viewModel: WishlistViewModel = viewModel()
) {
    val matches by viewModel.singleItemMatches.collectAsState()
    val uiState by viewModel.state.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()
    
    // 获取当前匹配的愿望清单项
    val targetWish = wishlist.firstOrNull { it.id == wishlistItemId }
    
    var minScore by remember { mutableStateOf(30.0) }
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(wishlistItemId) {
        // 注意：findMatchesForWishlistItem 已废弃，现在只匹配商品
        // 此功能暂时保留但不会返回结果
        viewModel.findMatchesForItem(wishlistItemId, minScore = minScore)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 顶部标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "匹配结果",
                    style = MaterialTheme.typography.headlineMedium
                )
                if (targetWish != null) {
                    Text(
                        text = "愿望: ${targetWish.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "筛选"
                    )
                }
                Button(onClick = { viewModel.findMatchesForItem(wishlistItemId, minScore = minScore) }) {
                    Text("刷新")
                }
                Button(onClick = onBack) {
                    Text("返回")
                }
            }
        }

        // 筛选面板
        if (showFilters) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "筛选条件",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "最低匹配分数: ${minScore.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = minScore.toFloat(),
                        onValueChange = { minScore = it.toDouble() },
                        valueRange = 0f..100f,
                        steps = 19
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.findMatchesForItem(wishlistItemId, minScore = minScore)
                                showFilters = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("应用")
                        }
                        OutlinedButton(
                            onClick = { showFilters = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("取消")
                        }
                    }
                }
            }
        }

        when (uiState) {
            is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("正在查找匹配...")
                    }
                }
            }
            is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (uiState as com.example.tradingplatform.ui.viewmodel.WishlistUiState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            else -> {
                if (matches.isEmpty()) {
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
                                text = "暂无匹配结果",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "注意：现在只匹配商品，不再使用愿望清单匹配",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // 显示匹配的愿望清单项信息
                    if (targetWish != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "你想买：${targetWish.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (targetWish.description.isNotEmpty()) {
                                    Text(
                                        text = targetWish.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                if (targetWish.minPrice > 0 || targetWish.maxPrice > 0) {
                                    val priceRange = when {
                                        targetWish.minPrice > 0 && targetWish.maxPrice > 0 ->
                                            "¥${String.format("%.2f", targetWish.minPrice)} - ¥${String.format("%.2f", targetWish.maxPrice)}"
                                        targetWish.minPrice > 0 ->
                                            "≥ ¥${String.format("%.2f", targetWish.minPrice)}"
                                        else ->
                                            "≤ ¥${String.format("%.2f", targetWish.maxPrice)}"
                                    }
                                    Text(
                                        text = "期望价格: $priceRange",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    Text(
                        text = "找到 ${matches.size} 个匹配结果",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(matches, key = { "${it.myItem.id}_${it.otherItem.id}" }) { match ->
                            ExchangeMatchCard(
                                match = match,
                                onMyItemClick = { onItemClick(match.myItem) },
                                onOtherItemClick = { onItemClick(match.otherItem) }
                            )
                        }
                    }
                }
            }
        }
    }
}



package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun ExchangeMatchesScreen(
    onBack: () -> Unit,
    onItemClick: (com.example.tradingplatform.data.items.Item) -> Unit,
    viewModel: WishlistViewModel = viewModel()
) {
    val matches by viewModel.matches.collectAsState()
    val uiState by viewModel.state.collectAsState()
    
    var minScore by remember { mutableStateOf(30.0) }
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (matches.isEmpty()) {
            viewModel.findMatches(minScore = minScore)
        }
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
            Text(
                text = "交换匹配",
                style = MaterialTheme.typography.headlineMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "筛选"
                    )
                }
                Button(onClick = { viewModel.findMatches(minScore = minScore) }) {
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
                                minScore = 30.0
                                viewModel.findMatches(minScore = minScore)
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
                                text = "尝试添加更多愿望清单或发布更多商品",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(matches, key = { "${it.wishlistItem.id}_${it.availableItem.id}" }) { match ->
                            ExchangeMatchCard(
                                match = match,
                                onItemClick = { onItemClick(match.availableItem) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExchangeMatchCard(
    match: ExchangeMatch,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onItemClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 匹配分数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "匹配度: ${match.matchScore.toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Divider()

            // 匹配类型标签
            if (match.isReverseMatch) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "🔄 反向匹配：有人想买你的商品",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // 愿望清单信息
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (match.isReverseMatch) "对方想买：" else "你想买：",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = match.wishlistItem.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (match.wishlistItem.description.isNotEmpty()) {
                    Text(
                        text = match.wishlistItem.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                // 显示价格区间（如果有）
                if (match.wishlistItem.minPrice > 0 || match.wishlistItem.maxPrice > 0) {
                    val priceRange = when {
                        match.wishlistItem.minPrice > 0 && match.wishlistItem.maxPrice > 0 ->
                            "¥${String.format("%.2f", match.wishlistItem.minPrice)} - ¥${String.format("%.2f", match.wishlistItem.maxPrice)}"
                        match.wishlistItem.minPrice > 0 ->
                            "≥ ¥${String.format("%.2f", match.wishlistItem.minPrice)}"
                        else ->
                            "≤ ¥${String.format("%.2f", match.wishlistItem.maxPrice)}"
                    }
                    Text(
                        text = "期望价格: $priceRange",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Divider()

            // 可交换商品信息
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (match.isReverseMatch) "你的商品：" else "可交换：",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (match.availableItem.imageUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(match.availableItem.imageUrl),
                            contentDescription = match.availableItem.title,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = match.availableItem.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (match.availableItem.description.isNotEmpty()) {
                            Text(
                                text = match.availableItem.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                        Text(
                            text = "¥${String.format("%.2f", match.availableItem.price)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (match.isReverseMatch) "买家: ${match.wishlistItem.userEmail}" else "卖家: ${match.availableItem.ownerEmail}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (match.availableItem.category.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = match.availableItem.category,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 匹配原因
            if (match.matchReasons.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    match.matchReasons.forEach { reason ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = reason,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}


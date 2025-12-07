package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.tradingplatform.data.wishlist.ExchangeMatch
import com.example.tradingplatform.ui.i18n.LocalAppLanguage
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppStrings
import com.example.tradingplatform.ui.viewmodel.WishlistViewModel

@Composable
fun ExchangeMatchesScreen(
    onBack: () -> Unit,
    onItemClick: (com.example.tradingplatform.data.items.Item) -> Unit,
    onSendMessage: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    viewModel: WishlistViewModel = viewModel()
) {
    val matches by viewModel.matches.collectAsState()
    val uiState by viewModel.state.collectAsState()
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    val isEnglish = lang == AppLanguage.EN
    
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
        // 顶部标题和返回按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：返回按钮和标题
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = strings.exchangeMatchesBack
                    )
                }
                Text(
                    text = strings.exchangeMatchesTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            // 右侧：筛选和刷新按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = strings.exchangeMatchesFilter
                    )
                }
                Button(
                    onClick = { viewModel.findMatches(minScore = minScore) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = strings.exchangeMatchesRefresh,
                        maxLines = 1
                    )
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
                        text = strings.exchangeMatchesFilterTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = java.lang.String.format(strings.exchangeMatchesMinScore, minScore.toInt()),
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
                            Text(strings.exchangeMatchesApply)
                        }
                        OutlinedButton(
                            onClick = { showFilters = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.exchangeMatchesCancel)
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
                        Text(strings.exchangeMatchesLoading)
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
                                text = strings.exchangeMatchesEmpty,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = strings.exchangeMatchesEmptySubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(matches, key = { "${it.myItem.id}_${it.otherItem.id}" }) { match ->
                            ExchangeMatchCard(
                                match = match,
                                onMyItemClick = { onItemClick(match.myItem) },
                                onOtherItemClick = { onItemClick(match.otherItem) },
                                onSendMessage = {
                                    // 导航到聊天界面，传递对方用户信息
                                    val receiverUid = match.otherItem.ownerUid.ifEmpty { "unknown" }
                                    val receiverEmail = match.otherItem.ownerEmail.ifEmpty { "unknown@example.com" }
                                    onSendMessage(receiverUid, receiverEmail, match.otherItem.id, match.otherItem.title)
                                }
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
    onMyItemClick: () -> Unit,
    onOtherItemClick: () -> Unit,
    onSendMessage: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier.fillMaxWidth()
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
                        text = java.lang.String.format(strings.exchangeMatchScore, match.matchScore.toInt()),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Divider()

            // 我的商品信息
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = strings.exchangeMatchMyItem,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    onClick = onMyItemClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (match.myItem.imageUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(match.myItem.imageUrl),
                                contentDescription = match.myItem.title,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = match.myItem.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (match.myItem.description.isNotEmpty()) {
                                Text(
                                    text = match.myItem.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                            Text(
                                text = "¥${String.format("%.2f", match.myItem.price)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            if (match.myItem.category.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = match.myItem.category,
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

            Divider()

            // 交换箭头
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = strings.exchangeMatchExchangeable,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider()

            // 其他用户的商品信息
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = strings.exchangeMatchOtherItem,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    onClick = onOtherItemClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (match.otherItem.imageUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(match.otherItem.imageUrl),
                                contentDescription = match.otherItem.title,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = match.otherItem.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (match.otherItem.description.isNotEmpty()) {
                                Text(
                                    text = match.otherItem.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                            Text(
                                text = "¥${String.format("%.2f", match.otherItem.price)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = java.lang.String.format(strings.exchangeMatchSeller, match.otherItem.ownerEmail),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (match.otherItem.category.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            text = match.otherItem.category,
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
            
            // 发消息按钮
            Button(
                onClick = onSendMessage,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.exchangeMatchSendMessage)
            }
        }
    }
}


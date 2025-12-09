package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradingplatform.data.auth.AuthRepository
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.data.timetable.RecommendationReasonType
import com.example.tradingplatform.data.timetable.RecommendedItem
import com.example.tradingplatform.ui.i18n.LocalAppStrings
import com.example.tradingplatform.ui.viewmodel.ItemViewModel

enum class MainScreenTab {
    HOME,      // 首页 / Home
    POST,      // 发布商品 / Post item
    MY,        // 我的 / My
    CAMERA     // 拍照识别 / Camera recognition
}

@Composable
fun MainScreen(
    onItemClick: (Item) -> Unit,
    onNavigateToMy: () -> Unit,
    onNavigateToPost: () -> Unit,
    onNavigateToCamera: () -> Unit,
    viewModel: ItemViewModel = viewModel()
) {
    // 内容区域 - 只显示首页内容 / Content area - only show home content
    HomeTab(
        onItemClick = onItemClick,
        viewModel = viewModel,
        onNavigateToMy = onNavigateToMy
    )
}

@Composable
fun HomeTab(
    onItemClick: (Item) -> Unit,
    viewModel: ItemViewModel,
    onNavigateToMy: () -> Unit
) {
    val items by viewModel.items.collectAsState()
    val uiState by viewModel.state.collectAsState()
    val recommendedItems by viewModel.recommendedItems.collectAsState()
    
    val context = LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    val strings = LocalAppStrings.current
    
    var currentUid by remember { mutableStateOf<String?>(null) }
    var currentEmail by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        currentUid = authRepo.getCurrentUserUid()
        currentEmail = authRepo.getCurrentUserEmail()
    }
    
    // 过滤掉当前用户发布的商品 / Filter out items posted by current user
    val filteredItems = remember(items, currentUid, currentEmail) {
        items.filter { item ->
            val uidMatch = currentUid?.let { item.ownerUid == it } ?: false
            val emailMatch = currentEmail?.let { 
                item.ownerEmail.equals(it, ignoreCase = true) 
            } ?: false
            // 只显示不是当前用户的商品 / Only show items that are not from current user
            !uidMatch && !emailMatch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 在首页标签页组合时加载基于课表的推荐 / Load timetable-based recommendations once when Home tab is composed
        LaunchedEffect(Unit) {
            viewModel.loadRecommendedItemsForCurrentStudent()
        }

        // 推荐教材部分（本地演示）/ Recommended textbooks section (local demo)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings.homeRecommendedSectionTitle,
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = { viewModel.loadRecommendedItemsForCurrentStudent() }) {
                Text(strings.homeRefreshRecommendationsButton)
            }
        }

        if (recommendedItems.isNotEmpty()) {
            val currentTermItems = mutableListOf<RecommendedItem>()
            val previewItems = mutableListOf<RecommendedItem>()
            val cetItems = mutableListOf<RecommendedItem>()

            for (rec in recommendedItems) {
                val types = rec.reasons.map { it.type }.toSet()
                val bucket = when {
                    RecommendationReasonType.CET_SEASON in types -> 0
                    RecommendationReasonType.CURRENT_TERM in types -> 1
                    RecommendationReasonType.PREVIEW_TERM in types -> 2
                    else -> null
                }

                when (bucket) {
                    0 -> cetItems.add(rec)
                    1 -> currentTermItems.add(rec)
                    2 -> previewItems.add(rec)
                }
            }

            val hasCurrent = currentTermItems.isNotEmpty()
            val hasPreview = previewItems.isNotEmpty()
            val hasCet = cetItems.isNotEmpty()

            if (hasCurrent || hasPreview || hasCet) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = strings.homeStudySummaryTitle,
                            style = MaterialTheme.typography.titleSmall
                        )

                        if (hasCurrent) {
                            Text(
                                text = strings.homeStudySummaryFocusCurrentTerm,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (hasPreview) {
                            Text(
                                text = strings.homeStudySummaryFocusPreview,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (hasCet) {
                            Text(
                                text = strings.homeStudySummaryFocusCet,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
            ) {
                if (currentTermItems.isNotEmpty()) {
                    item {
                        Text(
                            text = strings.homeReasonCurrentTerm,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(currentTermItems, key = { it.item.id }) { rec ->
                                RecommendedItemWithReasons(
                                    recommended = rec,
                                    onItemClick = onItemClick,
                                    strings = strings
                                )
                            }
                        }
                    }
                }

                if (previewItems.isNotEmpty()) {
                    item {
                        Text(
                            text = strings.homeReasonPreviewTerm,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(previewItems, key = { it.item.id }) { rec ->
                                RecommendedItemWithReasons(
                                    recommended = rec,
                                    onItemClick = onItemClick,
                                    strings = strings
                                )
                            }
                        }
                    }
                }

                if (cetItems.isNotEmpty()) {
                    item {
                        Text(
                            text = strings.homeReasonCetSeason,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(cetItems, key = { it.item.id }) { rec ->
                                RecommendedItemWithReasons(
                                    recommended = rec,
                                    onItemClick = onItemClick,
                                    strings = strings
                                )
                            }
                        }
                    }
                }
            }
            Divider()
        } else {
            Text(
                text = strings.homeNoRecommendationsHint,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            OutlinedButton(onClick = { onNavigateToMy() }) {
                Text(strings.homeGoToSettingsButton)
            }
            Divider(modifier = Modifier.padding(top = 8.dp))
        }

        when (val state = uiState) {
            is com.example.tradingplatform.ui.viewmodel.ItemUiState.Loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text(strings.homeLoadingText, modifier = Modifier.padding(start = 8.dp))
                }
            }
            is com.example.tradingplatform.ui.viewmodel.ItemUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                if (filteredItems.isEmpty()) {
                    Text(
                        text = strings.homeEmptyTip,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredItems, key = { it.id }) { item ->
                            ItemCard(item = item, onClick = { onItemClick(item) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendedItemWithReasons(
    recommended: RecommendedItem,
    onItemClick: (Item) -> Unit,
    strings: com.example.tradingplatform.ui.i18n.AppStrings
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.widthIn(max = 320.dp)
    ) {
        ItemCard(item = recommended.item, onClick = { onItemClick(recommended.item) })

        val reasonLabels = recommended.reasons.mapNotNull { reason ->
            when (reason.type) {
                RecommendationReasonType.CURRENT_TERM -> strings.homeReasonCurrentTerm
                RecommendationReasonType.PREVIEW_TERM -> strings.homeReasonPreviewTerm
                RecommendationReasonType.CET_SEASON -> strings.homeReasonCetSeason
            }
        }

        if (reasonLabels.isNotEmpty()) {
            Text(
                text = reasonLabels.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}


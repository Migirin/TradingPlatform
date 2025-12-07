package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.ui.components.CategorySelector
import com.example.tradingplatform.ui.viewmodel.WishlistViewModel
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppLanguage

@Composable
fun AddItemToWishlistScreen(
    item: Item,
    onDone: () -> Unit,
    viewModel: WishlistViewModel = viewModel()
) {
    val lang = LocalAppLanguage.current
    val isEnglish = lang == AppLanguage.EN
    val targetPrice = remember { mutableStateOf(item.price.toString()) }
    val enablePriceAlert = remember { mutableStateOf(true) }
    val uiState by viewModel.state.collectAsState()

    // 发布成功后返回
    LaunchedEffect(uiState is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Success) {
        if (uiState is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Success) {
            kotlinx.coroutines.delay(500)
            viewModel.resetState()
            onDone()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isEnglish) "Add to wishlist" else "加入愿望清单",
            style = MaterialTheme.typography.headlineSmall
        )

        // 商品信息预览
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
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (isEnglish)
                        "Current price: ¥${String.format("%.2f", item.price)}"
                    else
                        "当前价格: ¥${String.format("%.2f", item.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                if (item.category.isNotEmpty()) {
                    Text(
                        text = (if (isEnglish) "Category: " else "类别: ") + item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 降价提醒设置
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEnglish) "Price alert" else "降价提醒",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (isEnglish)
                                "Notify you when the price drops to your target price"
                            else
                                "当商品价格降至目标价格时通知您",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enablePriceAlert.value,
                        onCheckedChange = { enablePriceAlert.value = it }
                    )
                }

                if (enablePriceAlert.value) {
                    OutlinedTextField(
                        value = targetPrice.value,
                        onValueChange = { targetPrice.value = it },
                        label = { Text(if (isEnglish) "Target price*" else "目标价格*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Loading,
                        placeholder = {
                            val example = String.format("%.2f", item.price * 0.8)
                            Text(if (isEnglish) "e.g. $example" else "例如：$example")
                        }
                    )
                }
            }
        }

        when (val s = uiState) {
            is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Error -> {
                Text(
                    text = s.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            com.example.tradingplatform.ui.viewmodel.WishlistUiState.Loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        text = if (isEnglish) "Adding..." else "正在添加...",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            com.example.tradingplatform.ui.viewmodel.WishlistUiState.Success -> {
                Text(
                    text = if (isEnglish) "Added successfully!" else "添加成功！",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            else -> {}
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDone,
                modifier = Modifier.weight(1f),
                enabled = uiState !is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Loading
            ) {
                Text(if (isEnglish) "Cancel" else "取消")
            }
            Button(
                onClick = {
                    val target = if (enablePriceAlert.value) {
                        targetPrice.value.toDoubleOrNull() ?: item.price
                    } else {
                        0.0
                    }
                    viewModel.addWishlistItem(
                        title = item.title,
                        category = item.category,
                        minPrice = 0.0,
                        maxPrice = 0.0,
                        targetPrice = target,
                        itemId = item.id,
                        enablePriceAlert = enablePriceAlert.value && target > 0,
                        description = item.description
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = (!enablePriceAlert.value || targetPrice.value.toDoubleOrNull() != null) &&
                        uiState !is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Loading
            ) {
                Text("添加")
            }
        }
    }
}


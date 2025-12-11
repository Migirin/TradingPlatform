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
import com.example.tradingplatform.data.wishlist.WishlistItem
import com.example.tradingplatform.ui.components.CategorySelector
import com.example.tradingplatform.ui.viewmodel.WishlistViewModel
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppLanguage

@Composable
fun EditWishlistItemScreen(
    item: WishlistItem,
    onDone: () -> Unit,
    viewModel: WishlistViewModel = viewModel()
) {
    val title = remember { mutableStateOf(item.title) }
    val category = remember { mutableStateOf(item.category) }
    val minPrice = remember { mutableStateOf(if (item.minPrice > 0) item.minPrice.toString() else "") }
    val maxPrice = remember { mutableStateOf(if (item.maxPrice > 0) item.maxPrice.toString() else "") }
    val targetPrice = remember { mutableStateOf(if (item.targetPrice > 0) item.targetPrice.toString() else "") }
    val enablePriceAlert = remember { mutableStateOf(item.enablePriceAlert && item.targetPrice > 0) }
    val description = remember { mutableStateOf(item.description) }
    
    val uiState by viewModel.state.collectAsState()
    val lang = LocalAppLanguage.current
    val isEnglish = lang == AppLanguage.EN

    // 更新成功后返回 / Return after successful update
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
            text = if (isEnglish) "Edit wishlist item" else "编辑愿望清单",
            style = MaterialTheme.typography.headlineSmall
        )

        // 商品信息 / Item information
        OutlinedTextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text(if (isEnglish) "Title*" else "商品名称*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Loading,
            singleLine = true
        )

        // 类别选择 / Category selection
        CategorySelector(
            selectedCategory = category.value,
            onCategorySelected = { category.value = it },
            modifier = Modifier.fillMaxWidth()
        )

        // 价格范围 / Price range
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = minPrice.value,
                onValueChange = { minPrice.value = it },
                label = { Text(if (isEnglish) "Min price" else "最低价格") },
                modifier = Modifier.weight(1f),
                enabled = uiState !is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Loading,
                placeholder = { Text("0") }
            )
            OutlinedTextField(
                value = maxPrice.value,
                onValueChange = { maxPrice.value = it },
                label = { Text(if (isEnglish) "Max price" else "最高价格") },
                modifier = Modifier.weight(1f),
                enabled = uiState !is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Loading,
                placeholder = { Text("0") }
            )
        }

        // 降价提醒设置 / Price drop alert settings
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
                            Text(if (isEnglish) "e.g. 5000.00" else "例如：5000.00")
                        }
                    )
                }
            }
        }

        // 描述 / Description
        OutlinedTextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text(if (isEnglish) "Description" else "描述") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Loading,
            minLines = 3,
            maxLines = 5
        )

        // 错误信息 / Error message
        when (val s = uiState) {
            is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Error -> {
                Text(
                    text = if (isEnglish) "Error: ${s.message}" else "错误：${s.message}",
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
                    Text(if (isEnglish) "Updating..." else "正在更新...", modifier = Modifier.padding(start = 8.dp))
                }
            }
            com.example.tradingplatform.ui.viewmodel.WishlistUiState.Success -> {
                Text(
                    text = if (isEnglish) "Updated successfully!" else "更新成功！",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            else -> {}
        }

        // 按钮 / Buttons
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
                    val min = minPrice.value.toDoubleOrNull() ?: 0.0
                    val max = maxPrice.value.toDoubleOrNull() ?: 0.0
                    val target = if (enablePriceAlert.value) {
                        targetPrice.value.toDoubleOrNull() ?: 0.0
                    } else {
                        0.0
                    }
                    viewModel.updateWishlistItem(
                        itemId = item.id,
                        title = title.value,
                        category = category.value,
                        minPrice = min,
                        maxPrice = max,
                        targetPrice = target,
                        enablePriceAlert = enablePriceAlert.value && target > 0,
                        description = description.value
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = title.value.isNotEmpty() &&
                        (!enablePriceAlert.value || targetPrice.value.toDoubleOrNull() != null) &&
                        uiState !is com.example.tradingplatform.ui.viewmodel.WishlistUiState.Loading
            ) {
                Text(if (isEnglish) "Update" else "更新")
            }
        }
    }
}


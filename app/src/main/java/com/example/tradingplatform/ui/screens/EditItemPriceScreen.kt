package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.ui.viewmodel.ItemViewModel
import com.example.tradingplatform.ui.viewmodel.ItemUiState
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppLanguage

@Composable
fun EditItemPriceScreen(
    item: Item,
    onDone: () -> Unit,
    viewModel: ItemViewModel = viewModel()
) {
    var priceText by remember { mutableStateOf(item.price.toString()) }
    val uiState by viewModel.state.collectAsState()
    val lang = LocalAppLanguage.current
    val isEnglish = lang == AppLanguage.EN

    // 更新成功后返回 / Return after successful update
    LaunchedEffect(uiState) {
        if (uiState is ItemUiState.Success) {
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
            text = if (isEnglish) "Edit Price" else "编辑价格",
            style = MaterialTheme.typography.headlineSmall
        )

        // 商品信息卡片 / Item info card
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
                if (item.category.isNotEmpty()) {
                    Text(
                        text = (if (isEnglish) "Category: " else "类别: ") + item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = (if (isEnglish) "Current price: " else "当前价格: ") + "¥${String.format("%.2f", item.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 新价格输入 / New price input
        OutlinedTextField(
            value = priceText,
            onValueChange = { input ->
                // 只允许数字和小数点 / Only allow numbers and decimal point
                val filtered = input.filter { it.isDigit() || it == '.' }
                // 确保只有一个小数点 / Ensure only one decimal point
                val parts = filtered.split(".")
                priceText = if (parts.size > 2) {
                    parts[0] + "." + parts.drop(1).joinToString("")
                } else {
                    filtered
                }
            },
            label = { Text(if (isEnglish) "New Price (¥)*" else "新价格 (¥)*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is ItemUiState.Loading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            placeholder = { Text(if (isEnglish) "e.g. 99.00" else "例如：99.00") }
        )

        // 状态显示 / Status display
        when (val s = uiState) {
            is ItemUiState.Error -> {
                Text(
                    text = if (isEnglish) "Error: ${s.message}" else "错误：${s.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            ItemUiState.Loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        if (isEnglish) "Updating..." else "正在更新...",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            ItemUiState.Success -> {
                Text(
                    text = if (isEnglish) "Updated successfully!" else "更新成功！",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.weight(1f))

        // 按钮 / Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDone,
                modifier = Modifier.weight(1f),
                enabled = uiState !is ItemUiState.Loading
            ) {
                Text(if (isEnglish) "Cancel" else "取消")
            }
            Button(
                onClick = {
                    val newPrice = priceText.toDoubleOrNull()
                    if (newPrice != null && newPrice > 0) {
                        viewModel.updateItemPrice(item.id, newPrice)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = priceText.toDoubleOrNull()?.let { it > 0 } == true &&
                        uiState !is ItemUiState.Loading
            ) {
                Text(if (isEnglish) "Update" else "更新")
            }
        }
    }
}

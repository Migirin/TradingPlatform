package com.example.tradingplatform.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.example.tradingplatform.ui.components.CategorySelector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.tradingplatform.ui.viewmodel.ItemUiState
import com.example.tradingplatform.ui.viewmodel.ItemViewModel

@Composable
fun PostItemScreen(
    onDone: () -> Unit,
    viewModel: ItemViewModel = viewModel()
) {
    val title = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }
    val desc = remember { mutableStateOf("") }
    val story = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val vm = viewModel
    val uiState by vm.state.collectAsState()

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri.value = uri
    }

    // 发布成功后返回
    LaunchedEffect(uiState is ItemUiState.Success) {
        if (uiState is ItemUiState.Success) {
            // 延迟一下，让用户看到成功提示
            kotlinx.coroutines.delay(500)
            vm.resetState() // 先重置状态
            onDone() // 然后导航返回
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingValues(24.dp))
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "发布物品",
            style = MaterialTheme.typography.headlineSmall
        )

        // 图片选择
        if (imageUri.value != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri.value),
                contentDescription = "商品图片",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("选择图片（可选）")
            }
        }

        OutlinedTextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("标题*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is ItemUiState.Loading
        )

        OutlinedTextField(
            value = price.value,
            onValueChange = { price.value = it },
            label = { Text("价格*（数字）") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is ItemUiState.Loading
        )

        CategorySelector(
            selectedCategory = category.value,
            onCategorySelected = { category.value = it },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = desc.value,
            onValueChange = { desc.value = it },
            label = { Text("描述") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is ItemUiState.Loading,
            minLines = 3
        )

        OutlinedTextField(
            value = story.value,
            onValueChange = { story.value = it },
            label = { Text("商品故事（可选）") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is ItemUiState.Loading,
            minLines = 4,
            placeholder = { Text("分享这件商品背后的故事...\n例如：陪伴我大学四年的笔记本，见证了我的成长...") }
        )

        OutlinedTextField(
            value = phoneNumber.value,
            onValueChange = { phoneNumber.value = it },
            label = { Text("联系电话*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is ItemUiState.Loading,
            placeholder = { Text("例如：13800138000") }
        )

        when (val s = uiState) {
            is ItemUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "发布失败",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            ItemUiState.Loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("正在发布...", modifier = Modifier.padding(start = 8.dp))
                }
            }
            ItemUiState.Success -> {
                Text(
                    text = "发布成功！",
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
                enabled = uiState !is ItemUiState.Loading
            ) {
                Text("取消")
            }
            Button(
                onClick = {
                    val priceValue = price.value.toDoubleOrNull() ?: 0.0
                    if (title.value.isNotBlank() && priceValue > 0 && phoneNumber.value.isNotBlank()) {
                        vm.postItem(title.value, priceValue, category.value, desc.value, story.value, phoneNumber.value, imageUri.value)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = title.value.isNotBlank() 
                        && price.value.toDoubleOrNull() != null 
                        && price.value.toDoubleOrNull()!! > 0
                        && phoneNumber.value.isNotBlank()
                        && uiState !is ItemUiState.Loading
            ) {
                Text("发布")
            }
        }
    }
}

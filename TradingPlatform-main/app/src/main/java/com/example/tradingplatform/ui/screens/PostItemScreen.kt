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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppLanguage
import com.example.tradingplatform.ui.viewmodel.ItemUiState
import com.example.tradingplatform.ui.viewmodel.ItemViewModel

@Composable
fun PostItemScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit = onDone,
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
    val lang = LocalAppLanguage.current
    val isEnglish = lang == AppLanguage.EN

    // 图片选择器 / Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri.value = uri
    }

    // 发布成功后返回 / Return after successful post
    var hasNavigated by remember { mutableStateOf(false) }
    LaunchedEffect(uiState) {
        android.util.Log.d("PostItemScreen", "uiState changed: $uiState, hasNavigated: $hasNavigated")
        if (uiState is ItemUiState.Success && !hasNavigated) {
            hasNavigated = true
            android.util.Log.d("PostItemScreen", "发布成功，准备导航")
            // 延迟一下，让用户看到成功提示 / Delay a bit to let user see success message
            kotlinx.coroutines.delay(800)
            android.util.Log.d("PostItemScreen", "调用 onDone()")
            vm.resetState() // 先重置状态 / Reset state first
            onDone() // 然后导航返回 / Then navigate back
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
            text = if (isEnglish) "Post item" else "发布物品",
            style = MaterialTheme.typography.headlineSmall
        )

        // 图片选择 / Image selection
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
                Text(if (isEnglish) "Select image (optional)" else "选择图片（可选）")
            }
        }

        OutlinedTextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text(if (isEnglish) "Title*" else "标题*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is ItemUiState.Loading
        )

        OutlinedTextField(
            value = price.value,
            onValueChange = { price.value = it },
            label = { Text(if (isEnglish) "Price* (number)" else "价格*（数字）") },
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
            label = { Text(if (isEnglish) "Description" else "描述") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is ItemUiState.Loading,
            minLines = 3
        )

        OutlinedTextField(
            value = story.value,
            onValueChange = { story.value = it },
            label = { Text(if (isEnglish) "Item story (optional)" else "商品故事（可选）") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is ItemUiState.Loading,
            minLines = 4,
            placeholder = {
                Text(
                    if (isEnglish)
                        "Share the story behind this item...\nFor example: This laptop has been with me through four years of college..."
                    else
                        "分享这件商品背后的故事...\n例如：陪伴我大学四年的笔记本，见证了我的成长..."
                )
            }
        )

        OutlinedTextField(
            value = phoneNumber.value,
            onValueChange = { phoneNumber.value = it },
            label = { Text(if (isEnglish) "Phone number*" else "联系电话*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is ItemUiState.Loading,
            placeholder = { Text(if (isEnglish) "e.g. 13800138000" else "例如：13800138000") }
        )

        when (val s = uiState) {
            is ItemUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isEnglish) "Post failed" else "发布失败",
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
                    Text(if (isEnglish) "Posting..." else "正在发布...", modifier = Modifier.padding(start = 8.dp))
                }
            }
            ItemUiState.Success -> {
                Text(
                    text = if (isEnglish) "Posted successfully!" else "发布成功！",
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
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = uiState !is ItemUiState.Loading
            ) {
                Text(if (isEnglish) "Cancel" else "取消")
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
                Text(if (isEnglish) "Post" else "发布")
            }
        }
    }
}

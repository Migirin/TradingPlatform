package com.example.tradingplatform.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.ui.viewmodel.ItemViewModel

@Composable
fun ItemListScreen(
    onPostClick: () -> Unit,
    onChatClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onMyClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onItemClick: (Item) -> Unit,
    viewModel: ItemViewModel = viewModel()
) {
    val vm = viewModel
    val items by vm.items.collectAsState()
    val uiState by vm.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onPostClick, modifier = Modifier.weight(1f)) {
                Text("发布物品")
            }
            Button(onClick = onChatClick, modifier = Modifier.weight(1f)) {
                Text("我的消息")
            }
            Button(onClick = onWishlistClick, modifier = Modifier.weight(1f)) {
                Text("愿望清单")
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onMyClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("我的")
            }
            OutlinedButton(
                onClick = onCameraClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("📷 拍照识别")
            }
        }

        val currentState = uiState
        when (currentState) {
            is com.example.tradingplatform.ui.viewmodel.ItemUiState.Loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text("加载中...", modifier = Modifier.padding(start = 8.dp))
                }
            }
            is com.example.tradingplatform.ui.viewmodel.ItemUiState.Error -> {
                Text(
                    text = currentState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                if (items.isEmpty()) {
                    Text(
                        text = "暂无商品，点击上方按钮发布第一个商品吧！",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(items, key = { it.id }) { item ->
                            ItemCard(item = item, onClick = { onItemClick(item) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCard(item: Item, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    Log.d("ItemCard", "点击商品: ${item.id} - ${item.title}")
                    onClick()
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (item.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(item.imageUrl),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                )
            } else {
                // 占位图
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("无图片", style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "¥${String.format("%.2f", item.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description.take(50) + if (item.description.length > 50) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

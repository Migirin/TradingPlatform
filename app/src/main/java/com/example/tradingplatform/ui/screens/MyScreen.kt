package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.tradingplatform.data.auth.AuthRepository
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.data.wishlist.WishlistItem
import com.example.tradingplatform.ui.viewmodel.ItemViewModel
import com.example.tradingplatform.ui.viewmodel.WishlistViewModel

enum class MyScreenTab {
    MY_SOLD,    // 我出售的
    MY_BOUGHT,  // 我买到的
    WISHLIST    // 我的愿望清单
}

@Composable
fun MyScreen(
    onBack: () -> Unit = {},
    onItemClick: (Item) -> Unit,
    onWishlistItemMatch: (String) -> Unit = {},
    itemViewModel: ItemViewModel = viewModel(),
    wishlistViewModel: WishlistViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    
    var currentUid by remember { mutableStateOf<String?>(null) }
    var currentEmail by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        currentUid = authRepo.getCurrentUserUid()
        currentEmail = authRepo.getCurrentUserEmail()
        // 刷新商品列表，确保从 Supabase 获取最新数据
        itemViewModel.loadItems()
    }
    
    val items by itemViewModel.items.collectAsState()
    val wishlist by wishlistViewModel.wishlist.collectAsState()
    
    // 我出售的商品 - 同时匹配 ownerUid 和 ownerEmail
    val mySoldItems = remember(items, currentUid, currentEmail) {
        items.filter { item ->
            // 优先匹配 ownerUid，如果没有则匹配 ownerEmail
            val uidMatch = currentUid?.let { item.ownerUid == it } ?: false
            val emailMatch = currentEmail?.let { 
                item.ownerEmail.equals(it, ignoreCase = true) 
            } ?: false
            uidMatch || emailMatch
        }
    }
    
    // 我买到的商品（暂时为空，需要购买记录系统）
    val myBoughtItems = remember {
        emptyList<Item>()
    }
    
    var selectedTab by remember { mutableStateOf(MyScreenTab.MY_SOLD) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部用户信息
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "我的",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = currentEmail ?: "未登录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Button(onClick = onBack) {
                    Text("返回")
                }
            }
        }
        
        // 标签页选择
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            Tab(
                selected = selectedTab == MyScreenTab.MY_SOLD,
                onClick = { selectedTab = MyScreenTab.MY_SOLD },
                text = { Text("我出售的 (${mySoldItems.size})") }
            )
            Tab(
                selected = selectedTab == MyScreenTab.MY_BOUGHT,
                onClick = { selectedTab = MyScreenTab.MY_BOUGHT },
                text = { Text("我买到的 (${myBoughtItems.size})") }
            )
            Tab(
                selected = selectedTab == MyScreenTab.WISHLIST,
                onClick = { selectedTab = MyScreenTab.WISHLIST },
                text = { Text("我的愿望清单 (${wishlist.size})") }
            )
        }
        
        // 内容区域
        when (selectedTab) {
            MyScreenTab.MY_SOLD -> {
                MySoldItemsTab(
                    items = mySoldItems,
                    onItemClick = onItemClick
                )
            }
            MyScreenTab.MY_BOUGHT -> {
                MyBoughtItemsTab(
                    items = myBoughtItems
                )
            }
            MyScreenTab.WISHLIST -> {
                MyWishlistTab(
                    wishlist = wishlist,
                    onItemMatch = onWishlistItemMatch,
                    onDelete = { wishlistViewModel.deleteWishlistItem(it) }
                )
            }
        }
    }
}

@Composable
fun MySoldItemsTab(
    items: List<Item>,
    onItemClick: (Item) -> Unit
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "暂无出售的商品",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "去发布你的第一个商品吧！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { item ->
                ItemCard(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
fun MyBoughtItemsTab(
    items: List<Item>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "暂无购买记录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "购买功能即将上线",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MyWishlistTab(
    wishlist: List<WishlistItem>,
    onItemMatch: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (wishlist.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "愿望清单为空",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "在商品详情页添加愿望清单",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(wishlist, key = { it.id }) { item ->
                WishlistItemCard(
                    item = item,
                    onDelete = { onDelete(item.id) },
                    onFindMatches = { onItemMatch(item.id) }
                )
            }
        }
    }
}


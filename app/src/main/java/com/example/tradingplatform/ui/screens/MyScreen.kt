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
import com.example.tradingplatform.data.chat.ChatConversation
import com.example.tradingplatform.ui.viewmodel.ItemViewModel
import com.example.tradingplatform.ui.viewmodel.WishlistViewModel
import com.example.tradingplatform.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Locale

enum class MyScreenTab {
    MY_SOLD,    // 我出售的
    MY_BOUGHT,  // 我买到的
    WISHLIST,   // 我的愿望清单
    MY_MESSAGES, // 我的消息
    SETTINGS    // 设置
}

@Composable
fun MyScreen(
    onBack: () -> Unit = {},
    onItemClick: (Item) -> Unit,
    onExchangeMatch: () -> Unit = {},
    onWishlistItemMatch: (String) -> Unit = {},
    onChangePassword: () -> Unit = {},
    onChatWithUser: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    initialTab: MyScreenTab = MyScreenTab.MY_SOLD,
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
    
    var selectedTab by remember { mutableStateOf(initialTab) }
    
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
            Tab(
                selected = selectedTab == MyScreenTab.MY_MESSAGES,
                onClick = { selectedTab = MyScreenTab.MY_MESSAGES },
                text = { Text("我的消息") }
            )
            Tab(
                selected = selectedTab == MyScreenTab.SETTINGS,
                onClick = { selectedTab = MyScreenTab.SETTINGS },
                text = { Text("设置") }
            )
        }
        
        // 内容区域
        when (selectedTab) {
            MyScreenTab.MY_SOLD -> {
                MySoldItemsTab(
                    items = mySoldItems,
                    onItemClick = onItemClick,
                    onExchangeMatch = onExchangeMatch
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
            MyScreenTab.MY_MESSAGES -> {
                MyMessagesTab(
                    onChatWithUser = onChatWithUser
                )
            }
            MyScreenTab.SETTINGS -> {
                MySettingsTab(
                    onChangePassword = onChangePassword
                )
            }
        }
    }
}

@Composable
fun MySettingsTab(
    onChangePassword: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onChangePassword,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("更改密码")
        }
    }
}

@Composable
fun MySoldItemsTab(
    items: List<Item>,
    onItemClick: (Item) -> Unit,
    onExchangeMatch: () -> Unit
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 交换匹配按钮
            Button(
                onClick = onExchangeMatch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                enabled = items.isNotEmpty()
            ) {
                Text("交换匹配")
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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

@Composable
fun MyMessagesTab(
    onChatWithUser: (String, String, String, String) -> Unit,
    chatViewModel: ChatViewModel = viewModel()
) {
    val conversations by chatViewModel.conversations.collectAsState()
    
    LaunchedEffect(Unit) {
        chatViewModel.loadConversations()
    }
    
    // 对话列表（不显示顶部栏，因为已经在标签页中）
    if (conversations.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "暂无对话",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "在商品详情页可以联系卖家",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(conversations, key = { it.otherUserUid }) { conversation ->
                MyConversationCard(
                    conversation = conversation,
                    onClick = {
                        onChatWithUser(
                            conversation.otherUserUid,
                            conversation.otherUserEmail,
                            conversation.itemId ?: "",
                            conversation.itemTitle ?: ""
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun MyConversationCard(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.otherUserEmail,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (conversation.itemTitle != null && conversation.itemTitle!!.isNotEmpty()) {
                    Text(
                        text = "关于: ${conversation.itemTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (conversation.lastMessage != null && conversation.lastMessage!!.isNotEmpty()) {
                    Text(
                        text = conversation.lastMessage!!.take(50) + if (conversation.lastMessage!!.length > 50) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            if (conversation.lastMessageTime != null) {
                Text(
                    text = dateFormat.format(java.util.Date(conversation.lastMessageTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

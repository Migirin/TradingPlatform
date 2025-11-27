package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.tradingplatform.ui.viewmodel.ItemViewModel

enum class MainScreenTab {
    HOME,      // 首页
    POST,      // 发布商品
    MY,        // 我的
    CAMERA     // 拍照识别
}

@Composable
fun MainScreen(
    onItemClick: (Item) -> Unit,
    onNavigateToMy: () -> Unit,
    onNavigateToPost: () -> Unit,
    onNavigateToCamera: () -> Unit,
    viewModel: ItemViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(MainScreenTab.HOME) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 内容区域 - 只显示首页内容
        Box(modifier = Modifier.weight(1f)) {
            HomeTab(
                onItemClick = onItemClick,
                viewModel = viewModel
            )
        }
        
        // 底部导航栏
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                label = { Text("首页") },
                selected = selectedTab == MainScreenTab.HOME,
                onClick = { selectedTab = MainScreenTab.HOME }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Add, contentDescription = "发布商品") },
                label = { Text("发布商品") },
                selected = false,
                onClick = {
                    // 直接导航，不改变 selectedTab
                    onNavigateToPost()
                }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                label = { Text("我的") },
                selected = false,
                onClick = {
                    // 直接导航，不改变 selectedTab
                    onNavigateToMy()
                }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = "拍照识别") },
                label = { Text("拍照识别") },
                selected = false,
                onClick = {
                    // 直接导航，不改变 selectedTab
                    onNavigateToCamera()
                }
            )
        }
    }
}

@Composable
fun HomeTab(
    onItemClick: (Item) -> Unit,
    viewModel: ItemViewModel
) {
    val items by viewModel.items.collectAsState()
    val uiState by viewModel.state.collectAsState()
    
    val context = LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    
    var currentUid by remember { mutableStateOf<String?>(null) }
    var currentEmail by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        currentUid = authRepo.getCurrentUserUid()
        currentEmail = authRepo.getCurrentUserEmail()
    }
    
    // 过滤掉当前用户发布的商品
    val filteredItems = remember(items, currentUid, currentEmail) {
        items.filter { item ->
            val uidMatch = currentUid?.let { item.ownerUid == it } ?: false
            val emailMatch = currentEmail?.let { 
                item.ownerEmail.equals(it, ignoreCase = true) 
            } ?: false
            // 只显示不是当前用户的商品
            !uidMatch && !emailMatch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (val state = uiState) {
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
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                if (filteredItems.isEmpty()) {
                    Text(
                        text = "暂无商品，点击下方按钮发布第一个商品吧！",
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


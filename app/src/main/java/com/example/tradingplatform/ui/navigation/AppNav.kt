package com.example.tradingplatform.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.ui.screens.AuthScreen
import com.example.tradingplatform.ui.screens.ChatScreen
import com.example.tradingplatform.ui.screens.ItemDetailScreen
import com.example.tradingplatform.ui.screens.ItemListScreen
import com.example.tradingplatform.ui.screens.MainScreen
import com.example.tradingplatform.ui.screens.PostItemScreen
import com.example.tradingplatform.ui.screens.WishlistScreen
import com.example.tradingplatform.ui.screens.ExchangeMatchesScreen
import com.example.tradingplatform.ui.screens.SingleItemMatchesScreen
import com.example.tradingplatform.ui.screens.AddItemToWishlistScreen
import com.example.tradingplatform.ui.screens.EditWishlistItemScreen
import com.example.tradingplatform.ui.screens.EditItemPriceScreen
import com.example.tradingplatform.ui.screens.MyScreen
import com.example.tradingplatform.ui.screens.AchievementScreen
import com.example.tradingplatform.ui.screens.CameraScreen
import com.example.tradingplatform.ui.screens.ChangePasswordScreen
import com.example.tradingplatform.ui.screens.RecognitionResultScreen
import com.example.tradingplatform.ui.screens.AllItemsScreen
import com.example.tradingplatform.data.vision.ApiConfig
import com.example.tradingplatform.ui.i18n.LocalAppStrings
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppLanguage
import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.example.tradingplatform.ui.viewmodel.ItemViewModel
import com.example.tradingplatform.data.auth.AuthRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.example.tradingplatform.ui.theme.LowSaturationRed
import androidx.navigation.compose.currentBackStackEntryAsState

object Routes {
    const val AUTH = "auth"
    const val ITEMS = "items"
    const val POST = "post"
    const val CHAT = "chat"
    const val CHAT_WITH_USER = "chat/{receiverUid}/{receiverEmail}/{itemId}/{itemTitle}"
    const val ITEM_DETAIL = "item_detail/{itemId}"
    const val WISHLIST = "wishlist"
    const val EXCHANGE_MATCHES = "exchange_matches"
    const val SINGLE_ITEM_MATCHES = "single_item_matches/{wishlistItemId}"
    const val ADD_ITEM_TO_WISHLIST = "add_item_to_wishlist/{itemId}"
    const val EDIT_WISHLIST_ITEM = "edit_wishlist_item/{itemId}"
    const val EDIT_ITEM_PRICE = "edit_item_price/{itemId}"
    const val ACHIEVEMENTS = "achievements"
    const val CAMERA = "camera"
    const val RECOGNITION_RESULT = "recognition_result"
    const val MY = "my"
    const val MY_SOLD = "my_sold"
    const val CHANGE_PASSWORD = "change_password"
    const val ALL_ITEMS = "all_items"
    
    fun addItemToWishlist(itemId: String) = "add_item_to_wishlist/$itemId"
    fun chatWithUser(receiverUid: String, receiverEmail: String, itemId: String = "", itemTitle: String = "") = 
        "chat/$receiverUid/$receiverEmail/$itemId/$itemTitle"
    
    fun itemDetail(itemId: String) = "item_detail/$itemId"
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.AUTH,
    modifier: Modifier = Modifier
) {
    // 共享的 ViewModel 实例（在导航作用域外创建）/ Shared ViewModel instance (created outside navigation scope)
    val sharedViewModel: ItemViewModel = viewModel()
    val wishlistViewModel: com.example.tradingplatform.ui.viewmodel.WishlistViewModel = viewModel()
    val context = LocalContext.current
    val authRepo = AuthRepository(context)
    val strings = LocalAppStrings.current
    
    // 使用 LaunchedEffect 异步获取开发者模式状态 / Use LaunchedEffect to asynchronously get developer mode status
    var isDevMode by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isDevMode = !authRepo.isLoggedIn()
    }
    
    // 获取当前路由 / Get current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination
    
    // 判断是否应该显示底部导航栏（登录页面不显示）/ Determine if bottom navigation bar should be shown (not shown on login page)
    val shouldShowBottomBar = currentRoute != Routes.AUTH
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    navController = navController
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
        composable(Routes.AUTH) { 
            AuthScreen(onAuthenticated = { 
                navController.navigate(Routes.ITEMS) { 
                    popUpTo(Routes.AUTH) { inclusive = true } 
                } 
            }) 
        }
        composable(Routes.ITEMS) { 
            MainScreen(
                viewModel = sharedViewModel,
                wishlistViewModel = wishlistViewModel,
                onItemClick = { item ->
                    Log.d("AppNav", "点击商品，准备导航: ${item.id} - ${item.title}")
                    sharedViewModel.setSelectedItem(item) // 保存选中的商品 / Save selected item
                    try {
                        val route = Routes.itemDetail(item.id)
                        Log.d("AppNav", "导航到: $route")
                        navController.navigate(route)
                        Log.d("AppNav", "导航成功")
                    } catch (e: Exception) {
                        Log.e("AppNav", "导航失败", e)
                    }
                },
                onNavigateToMy = { navController.navigate(Routes.MY) },
                onNavigateToPost = { navController.navigate(Routes.POST) },
                onNavigateToCamera = { navController.navigate(Routes.CAMERA) }
            )
        }
        composable(Routes.POST) { 
            val coroutineScope = rememberCoroutineScope()
            PostItemScreen(
                viewModel = sharedViewModel,
                onDone = { 
                    // 发布成功后，导航到"我的"界面的"我出售的"标签页 / After successful post, navigate to "My" screen's "My Sold" tab
                    Log.d("AppNav", "发布成功，准备导航到 MY_SOLD")
                    // 先刷新商品列表 / Refresh item list first
                    sharedViewModel.loadItems()
                    // 在协程中执行导航，确保在正确的上下文中 / Execute navigation in coroutine to ensure correct context
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(100) // 稍微延迟确保状态更新完成 / Slight delay to ensure state update completes
                        try {
                            navController.navigate(Routes.MY_SOLD) {
                                // 清除返回栈直到主界面（不包括主界面）/ Clear back stack until main screen (excluding main screen)
                                popUpTo(Routes.ITEMS) { inclusive = false }
                                // 避免重复添加相同的目标 / Avoid duplicate addition of same destination
                                launchSingleTop = true
                            }
                            Log.d("AppNav", "导航到 MY_SOLD 完成")
                        } catch (e: Exception) {
                            Log.e("AppNav", "导航失败", e)
                            // 如果导航失败，至少返回到主界面 / If navigation fails, at least return to main screen
                            navController.popBackStack(Routes.ITEMS, inclusive = false)
                        }
                    }
                },
                onCancel = {
                    // 点击取消时，返回到首页 / When cancel is clicked, return to home page
                    Log.d("AppNav", "取消发布，返回到首页")
                    navController.popBackStack(Routes.ITEMS, inclusive = false)
                }
            ) 
        }
        composable(Routes.CHAT) { 
            ChatScreen(
                onBack = { navController.popBackStack() },
                onConversationClick = { receiverUid, receiverEmail, itemId, itemTitle ->
                    navController.navigate(Routes.chatWithUser(receiverUid, receiverEmail, itemId, itemTitle))
                }
            ) 
        }
        composable(Routes.ALL_ITEMS) {
            AllItemsScreen(
                viewModel = sharedViewModel,
                onItemClick = { item ->
                    Log.d("AppNav", "从商品列表点击商品: ${item.id} - ${item.title}")
                    sharedViewModel.setSelectedItem(item)
                    navController.navigate(Routes.itemDetail(item.id))
                }
            )
        }
        composable(
            route = Routes.CHAT_WITH_USER,
            arguments = listOf(
                navArgument("receiverUid") { type = NavType.StringType },
                navArgument("receiverEmail") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType },
                navArgument("itemTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val receiverUid = backStackEntry.arguments?.getString("receiverUid") ?: ""
            val receiverEmail = backStackEntry.arguments?.getString("receiverEmail") ?: ""
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            val itemTitle = backStackEntry.arguments?.getString("itemTitle") ?: ""
            ChatScreen(
                onBack = { navController.popBackStack() },
                receiverUid = receiverUid.ifEmpty { null },
                receiverEmail = receiverEmail.ifEmpty { null },
                itemId = itemId.ifEmpty { null },
                itemTitle = itemTitle.ifEmpty { null }
            )
        }
        composable(Routes.WISHLIST) {
            val wishlistViewModel: com.example.tradingplatform.ui.viewmodel.WishlistViewModel = viewModel()
            WishlistScreen(
                onBack = { navController.popBackStack() },
                onFindMatches = {}, // 已移除，不再使用 / Removed, no longer used
                onFindMatchesForItem = { wishlistItemId ->
                    navController.navigate("single_item_matches/$wishlistItemId")
                },
                onEditItem = { itemId ->
                    navController.navigate("edit_wishlist_item/$itemId")
                },
                onAchievementsClick = { navController.navigate(Routes.ACHIEVEMENTS) },
                viewModel = wishlistViewModel
            )
        }
        composable(Routes.ACHIEVEMENTS) {
            AchievementScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.CAMERA) {
            val strings = LocalAppStrings.current
            var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var recognitionType by remember { 
                mutableStateOf(com.example.tradingplatform.ui.viewmodel.RecognitionType.ML_KIT_DEVICE) 
            }
            
            if (capturedBitmap == null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraScreen(
                        onBack = { navController.popBackStack() },
                        onResult = { bitmap ->
                            capturedBitmap = bitmap
                        },
                        recognitionType = recognitionType
                    )
                    
                    // 识别模式选择（浮动在相机预览上方）/ Recognition mode selection (floating above camera preview)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(strings.cameraSelectRecognitionMethod, style = MaterialTheme.typography.titleSmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { recognitionType = com.example.tradingplatform.ui.viewmodel.RecognitionType.ML_KIT_DEVICE },
                                    modifier = Modifier.weight(1f),
                                    colors = if (recognitionType == com.example.tradingplatform.ui.viewmodel.RecognitionType.ML_KIT_DEVICE) 
                                        ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text(strings.cameraDeviceSide, style = MaterialTheme.typography.labelSmall)
                                }
                                Button(
                                    onClick = { recognitionType = com.example.tradingplatform.ui.viewmodel.RecognitionType.THIRD_PARTY_BAIDU },
                                    modifier = Modifier.weight(1f),
                                    enabled = ApiConfig.isBaiduConfigured(),
                                    colors = if (recognitionType == com.example.tradingplatform.ui.viewmodel.RecognitionType.THIRD_PARTY_BAIDU) 
                                        ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text(strings.cameraBaiduAI, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            
                            // 如果选择了未配置的选项，自动切换到设备端 / If unconfigured option is selected, automatically switch to device-side
                            LaunchedEffect(recognitionType) {
                                when (recognitionType) {
                                    com.example.tradingplatform.ui.viewmodel.RecognitionType.THIRD_PARTY_BAIDU -> {
                                        if (!ApiConfig.isBaiduConfigured()) {
                                            recognitionType = com.example.tradingplatform.ui.viewmodel.RecognitionType.ML_KIT_DEVICE
                                        }
                                    }
                                    com.example.tradingplatform.ui.viewmodel.RecognitionType.THIRD_PARTY_TENCENT -> {
                                        if (!ApiConfig.isTencentConfigured()) {
                                            recognitionType = com.example.tradingplatform.ui.viewmodel.RecognitionType.ML_KIT_DEVICE
                                        }
                                    }
                                    else -> { /* 其他选项正常 / Other options are normal */ }
                                }
                            }
                        }
                    }
                }
            } else {
                capturedBitmap?.let { bitmap ->
                    RecognitionResultScreen(
                        capturedImage = bitmap,
                        onBack = {
                            capturedBitmap = null
                            navController.popBackStack()
                        },
                        onItemClick = { item ->
                            navController.navigate("item_detail/${item.id}")
                        }
                    )
                }
            }
        }
        composable(Routes.EXCHANGE_MATCHES) {
            ExchangeMatchesScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { item ->
                    sharedViewModel.setSelectedItem(item)
                    navController.navigate(Routes.itemDetail(item.id))
                }
            )
        }
        
        composable("single_item_matches/{wishlistItemId}") { backStackEntry ->
            val wishlistItemId = backStackEntry.arguments?.getString("wishlistItemId") ?: ""
            SingleItemMatchesScreen(
                wishlistItemId = wishlistItemId,
                onBack = { navController.popBackStack() },
                onItemClick = { item ->
                    sharedViewModel.setSelectedItem(item)
                    navController.navigate(Routes.itemDetail(item.id))
                }
            )
        }
        
        composable(Routes.MY) {
            MyScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { item ->
                    sharedViewModel.setSelectedItem(item)
                    navController.navigate(Routes.itemDetail(item.id))
                },
                onExchangeMatch = { navController.navigate(Routes.EXCHANGE_MATCHES) },
                onWishlistItemMatch = { wishlistItemId ->
                    navController.navigate("single_item_matches/$wishlistItemId")
                },
                onEditWishlistItem = { itemId ->
                    navController.navigate("edit_wishlist_item/$itemId")
                },
                onEditItemPrice = { itemId ->
                    navController.navigate("edit_item_price/$itemId")
                },
                onChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
                onChatWithUser = { receiverUid, receiverEmail, itemId, itemTitle ->
                    navController.navigate(Routes.chatWithUser(receiverUid, receiverEmail, itemId, itemTitle))
                },
                initialTab = com.example.tradingplatform.ui.screens.MyScreenTab.MY_SOLD
            )
        }
        composable(Routes.MY_SOLD) {
            MyScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { item ->
                    sharedViewModel.setSelectedItem(item)
                    navController.navigate(Routes.itemDetail(item.id))
                },
                onExchangeMatch = { navController.navigate(Routes.EXCHANGE_MATCHES) },
                onWishlistItemMatch = { wishlistItemId ->
                    navController.navigate("single_item_matches/$wishlistItemId")
                },
                onEditWishlistItem = { itemId ->
                    navController.navigate("edit_wishlist_item/$itemId")
                },
                onEditItemPrice = { itemId ->
                    navController.navigate("edit_item_price/$itemId")
                },
                onChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
                onChatWithUser = { receiverUid, receiverEmail, itemId, itemTitle ->
                    navController.navigate(Routes.chatWithUser(receiverUid, receiverEmail, itemId, itemTitle))
                },
                initialTab = com.example.tradingplatform.ui.screens.MyScreenTab.MY_SOLD
            )
        }
        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.ADD_ITEM_TO_WISHLIST,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            val item = itemId?.let { sharedViewModel.getItemById(it) }
            if (item != null) {
                AddItemToWishlistScreen(
                    item = item,
                    onDone = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                val isEnglish = LocalAppLanguage.current == AppLanguage.EN
                Text(if (isEnglish) "Item not found" else "商品不存在")
            }
        }
        composable(
            route = Routes.EDIT_WISHLIST_ITEM,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            val wishlistViewModel: com.example.tradingplatform.ui.viewmodel.WishlistViewModel = viewModel()
            val wishlist by wishlistViewModel.wishlist.collectAsState()
            val isEnglish = LocalAppLanguage.current == AppLanguage.EN
            
            // 添加加载状态跟踪，等待数据加载完成 / Add loading state tracking, wait for data to load
            var hasWaited by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                // 等待一小段时间让 Flow 发出数据 / Wait a short time for Flow to emit data
                kotlinx.coroutines.delay(500)
                hasWaited = true
            }
            
            val wishlistItem = remember(itemId, wishlist) {
                wishlist.firstOrNull { it.id == itemId }
            }
            
            when {
                wishlistItem != null -> {
                    EditWishlistItemScreen(
                        item = wishlistItem,
                        onDone = { navController.popBackStack() },
                        viewModel = wishlistViewModel
                    )
                }
                !hasWaited -> {
                    // 显示加载中状态 / Show loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(if (isEnglish) "Loading..." else "加载中...")
                        }
                    }
                }
                else -> {
                    // 数据加载完成但找不到项目 / Data loaded but item not found
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                    Text(if (isEnglish) "Wishlist item not found" else "愿望清单项不存在")
                }
            }
        }
        composable(
            route = Routes.EDIT_ITEM_PRICE,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            val isEnglish = LocalAppLanguage.current == AppLanguage.EN
            
            // 添加加载状态跟踪 / Add loading state tracking
            var hasWaited by remember { mutableStateOf(false) }
            var item by remember { mutableStateOf<com.example.tradingplatform.data.items.Item?>(null) }
            val items by sharedViewModel.items.collectAsState()
            
            LaunchedEffect(itemId) {
                Log.d("AppNav", "编辑价格页面 - itemId: $itemId")
                Log.d("AppNav", "编辑价格页面 - items 数量: ${items.size}")
                
                // 先从当前列表查找 / First search in current list
                item = items.firstOrNull { it.id == itemId }
                Log.d("AppNav", "编辑价格页面 - 从列表查找: ${item?.title}")
                
                // 如果找不到，重新加载所有商品 / If not found, reload all items
                if (item == null) {
                    Log.d("AppNav", "编辑价格页面 - 重新加载所有商品")
                    sharedViewModel.loadAllItems()
                    kotlinx.coroutines.delay(1000)
                    item = sharedViewModel.items.value.firstOrNull { it.id == itemId }
                    Log.d("AppNav", "编辑价格页面 - 重新加载后查找: ${item?.title}")
                }
                
                hasWaited = true
            }
            
            when {
                item != null -> {
                    EditItemPriceScreen(
                        item = item!!,
                        onDone = { navController.popBackStack() },
                        viewModel = sharedViewModel
                    )
                }
                !hasWaited -> {
                    // 显示加载中状态 / Show loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(if (isEnglish) "Loading..." else "加载中...")
                        }
                    }
                }
                else -> {
                    // 数据加载完成但找不到商品 / Data loaded but item not found
                    Log.w("AppNav", "编辑价格页面 - 找不到商品: $itemId")
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                    Text(if (isEnglish) "Item not found" else "商品不存在")
                }
            }
        }
        composable(
            route = Routes.ITEM_DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            // 优先使用保存的选中商品 / Prefer using saved selected item
            var item = sharedViewModel.getSelectedItem()
            Log.d("AppNav", "详情页 - 保存的商品: ${item?.id} - ${item?.title}")
            
            // 如果保存的商品不匹配，尝试通过ID查找 / If saved item doesn't match, try to find by ID
            val itemId = backStackEntry.arguments?.getString("itemId")
            Log.d("AppNav", "详情页 - 路由中的 itemId: $itemId")
            
            if (item == null || (itemId != null && item.id != itemId)) {
                item = itemId?.let { 
                    val found = sharedViewModel.getItemById(it)
                    Log.d("AppNav", "通过ID查找商品: $it -> ${found?.title}")
                    found
                }
            }
            
            if (item == null) {
                Log.w("AppNav", "找不到商品，返回列表页")
                // 如果找不到商品，返回列表页 / If item not found, return to list page
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                // 显示一个临时提示 / Show a temporary message
                val isEnglish = LocalAppLanguage.current == AppLanguage.EN
                Text(if (isEnglish) "Returning..." else "正在返回...")
                    } else {
                        Log.d("AppNav", "显示商品详情: ${item.id} - ${item.title}")
                        // 检查商品是否属于当前用户 / Check if item belongs to current user
                        var currentUid by remember { mutableStateOf<String?>(null) }
                        var currentEmail by remember { mutableStateOf<String?>(null) }
                        
                        LaunchedEffect(Unit) {
                            currentUid = authRepo.getCurrentUserUid()
                            currentEmail = authRepo.getCurrentUserEmail()
                        }
                        
                        val isOwnItem = remember(item, currentUid, currentEmail) {
                            val uidMatch = currentUid?.let { item.ownerUid == it } ?: false
                            val emailMatch = currentEmail?.let { 
                                item.ownerEmail.equals(it, ignoreCase = true) 
                            } ?: false
                            uidMatch || emailMatch
                        }
                        
                        ItemDetailScreen(
                            item = item,
                            onBack = { navController.popBackStack() },
                            onContact = { 
                                // 传递卖家信息到聊天界面 / Pass seller information to chat screen
                                val sellerUid = item.ownerUid.ifEmpty { "unknown" }
                                val sellerEmail = item.ownerEmail.ifEmpty { "unknown@example.com" }
                                navController.navigate(Routes.chatWithUser(sellerUid, sellerEmail, item.id, item.title))
                            },
                            onAddToWishlist = { navController.navigate(Routes.addItemToWishlist(item.id)) },
                            onDelete = {
                                // 开发者模式可以删除 / Developer mode can delete
                                sharedViewModel.deleteItem(item.id)
                                navController.popBackStack()
                            },
                            isDevMode = isDevMode,
                            isOwnItem = isOwnItem
                        )
                    }
        }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    navController: NavHostController
) {
    val strings = LocalAppStrings.current
    
    // 根据当前路由确定选中的标签 / Determine selected tab based on current route
    val isHomeSelected = currentRoute == Routes.ITEMS
    val isMessagesSelected = currentRoute == Routes.CHAT || (currentRoute?.startsWith("chat/") == true)
    val isPostSelected = currentRoute == Routes.POST
    val isMySelected = currentRoute == Routes.MY || currentRoute == Routes.MY_SOLD
    val isCameraSelected = currentRoute == Routes.CAMERA || currentRoute == Routes.RECOGNITION_RESULT
    val isAllItemsSelected = currentRoute == Routes.ALL_ITEMS
    
    NavigationBar(
        containerColor = LowSaturationRed
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = strings.bottomHomeLabel) },
            label = { Text(strings.bottomHomeLabel) },
            selected = isHomeSelected,
            onClick = {
                if (!isHomeSelected) {
                    navController.navigate(Routes.ITEMS) {
                        // 清除返回栈直到主界面（不包括主界面），避免重复 / Clear back stack until main screen (excluding main screen), avoid duplicates
                        popUpTo(Routes.ITEMS) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = strings.bottomAllItemsLabel) },
            label = { Text(strings.bottomAllItemsLabel) },
            selected = isAllItemsSelected,
            onClick = {
                navController.navigate(Routes.ALL_ITEMS) {
                    // 如果已经在ALL_ITEMS页面，不重复导航 / If already on ALL_ITEMS page, don't navigate again
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = strings.bottomPostLabel) },
            label = { Text(strings.bottomPostLabel) },
            selected = isPostSelected,
            onClick = {
                navController.navigate(Routes.POST) {
                    // 如果已经在POST页面，不重复导航 / If already on POST page, don't navigate again
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(MessageIcon, contentDescription = strings.bottomMessagesLabel) },
            label = { Text(strings.bottomMessagesLabel) },
            selected = isMessagesSelected,
            onClick = {
                if (!isMessagesSelected) {
                    navController.navigate(Routes.CHAT) {
                        // 如果已经在CHAT页面，不重复导航 / If already on CHAT page, don't navigate again
                        launchSingleTop = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = strings.bottomCameraLabel) },
            label = { Text(strings.bottomCameraLabel) },
            selected = isCameraSelected,
            onClick = {
                navController.navigate(Routes.CAMERA) {
                    // 如果已经在CAMERA页面，不重复导航 / If already on CAMERA page, don't navigate again
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = strings.bottomMyLabel) },
            label = { Text(strings.bottomMyLabel) },
            selected = isMySelected,
            onClick = {
                navController.navigate(Routes.MY) {
                    // 如果已经在MY页面，不重复导航 / If already on MY page, don't navigate again
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = Color.Transparent
            )
        )
    }
}

// 自定义消息图标（消息气泡）/ Custom message icon (message bubble)
val MessageIcon: ImageVector = androidx.compose.ui.graphics.vector.ImageVector.Builder(
    name = "Message",
    defaultWidth = 24.0.dp,
    defaultHeight = 24.0.dp,
    viewportWidth = 24.0f,
    viewportHeight = 24.0f
).apply {
    path(
        fill = androidx.compose.ui.graphics.SolidColor(Color.Black),
        fillAlpha = 1f,
        stroke = null,
        strokeAlpha = 1f,
        strokeLineWidth = 1.0f,
        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
        strokeLineMiter = 1f,
        pathFillType = PathFillType.NonZero
    ) {
        // 消息气泡图标路径 / Message bubble icon path
        moveTo(20.0f, 2.0f)
        horizontalLineTo(4.0f)
        curveTo(2.9f, 2.0f, 2.0f, 2.9f, 2.0f, 4.0f)
        verticalLineTo(22.0f)
        lineTo(6.0f, 18.0f)
        horizontalLineTo(20.0f)
        curveTo(21.1f, 18.0f, 22.0f, 17.1f, 22.0f, 16.0f)
        verticalLineTo(4.0f)
        curveTo(22.0f, 2.9f, 21.1f, 2.0f, 20.0f, 2.0f)
        close()
        moveTo(20.0f, 16.0f)
        horizontalLineTo(6.0f)
        lineTo(4.0f, 18.0f)
        verticalLineTo(4.0f)
        horizontalLineTo(20.0f)
        verticalLineTo(16.0f)
        close()
    }
}.build()




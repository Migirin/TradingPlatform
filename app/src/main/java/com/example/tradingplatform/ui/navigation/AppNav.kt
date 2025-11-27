package com.example.tradingplatform.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.example.tradingplatform.ui.screens.MyScreen
import com.example.tradingplatform.ui.screens.AchievementScreen
import com.example.tradingplatform.ui.screens.CameraScreen
import com.example.tradingplatform.ui.screens.ChangePasswordScreen
import com.example.tradingplatform.ui.screens.RecognitionResultScreen
import com.example.tradingplatform.data.vision.ApiConfig
import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.example.tradingplatform.ui.viewmodel.ItemViewModel
import com.example.tradingplatform.data.auth.AuthRepository

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
    const val ACHIEVEMENTS = "achievements"
    const val CAMERA = "camera"
    const val RECOGNITION_RESULT = "recognition_result"
    const val MY = "my"
    const val MY_SOLD = "my_sold"
    const val CHANGE_PASSWORD = "change_password"
    
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
    // 共享的 ViewModel 实例（在导航作用域外创建）
    val sharedViewModel: ItemViewModel = viewModel()
    val context = LocalContext.current
    val authRepo = AuthRepository(context)
    
    // 使用 LaunchedEffect 异步获取开发者模式状态
    var isDevMode by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isDevMode = !authRepo.isLoggedIn()
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
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
                onItemClick = { item ->
                    Log.d("AppNav", "点击商品，准备导航: ${item.id} - ${item.title}")
                    sharedViewModel.setSelectedItem(item) // 保存选中的商品
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
                    // 发布成功后，导航到"我的"界面的"我出售的"标签页
                    Log.d("AppNav", "发布成功，准备导航到 MY_SOLD")
                    // 先刷新商品列表
                    sharedViewModel.loadItems()
                    // 在协程中执行导航，确保在正确的上下文中
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(100) // 稍微延迟确保状态更新完成
                        try {
                            navController.navigate(Routes.MY_SOLD) {
                                // 清除返回栈直到主界面（不包括主界面）
                                popUpTo(Routes.ITEMS) { inclusive = false }
                                // 避免重复添加相同的目标
                                launchSingleTop = true
                            }
                            Log.d("AppNav", "导航到 MY_SOLD 完成")
                        } catch (e: Exception) {
                            Log.e("AppNav", "导航失败", e)
                            // 如果导航失败，至少返回到主界面
                            navController.popBackStack(Routes.ITEMS, inclusive = false)
                        }
                    }
                },
                onCancel = {
                    // 点击取消时，返回到首页
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
            WishlistScreen(
                onBack = { navController.popBackStack() },
                onFindMatches = {}, // 已移除，不再使用
                onFindMatchesForItem = { wishlistItemId ->
                    navController.navigate("single_item_matches/$wishlistItemId")
                },
                onAchievementsClick = { navController.navigate(Routes.ACHIEVEMENTS) }
            )
        }
        composable(Routes.ACHIEVEMENTS) {
            AchievementScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.CAMERA) {
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
                    
                    // 识别模式选择（浮动在相机预览上方）
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
                            Text("选择识别方式：", style = MaterialTheme.typography.titleSmall)
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
                                    Text("设备端", style = MaterialTheme.typography.labelSmall)
                                }
                                Button(
                                    onClick = { recognitionType = com.example.tradingplatform.ui.viewmodel.RecognitionType.THIRD_PARTY_BAIDU },
                                    modifier = Modifier.weight(1f),
                                    enabled = ApiConfig.isBaiduConfigured(),
                                    colors = if (recognitionType == com.example.tradingplatform.ui.viewmodel.RecognitionType.THIRD_PARTY_BAIDU) 
                                        ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("百度AI", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            
                            // 如果选择了未配置的选项，自动切换到设备端
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
                                    else -> { /* 其他选项正常 */ }
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
                Text("商品不存在")
            }
        }
        composable(
            route = Routes.ITEM_DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            // 优先使用保存的选中商品
            var item = sharedViewModel.getSelectedItem()
            Log.d("AppNav", "详情页 - 保存的商品: ${item?.id} - ${item?.title}")
            
            // 如果保存的商品不匹配，尝试通过ID查找
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
                // 如果找不到商品，返回列表页
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                // 显示一个临时提示
                Text("正在返回...")
                    } else {
                        Log.d("AppNav", "显示商品详情: ${item.id} - ${item.title}")
                        // 检查商品是否属于当前用户
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
                                // 传递卖家信息到聊天界面
                                val sellerUid = item.ownerUid.ifEmpty { "unknown" }
                                val sellerEmail = item.ownerEmail.ifEmpty { "unknown@example.com" }
                                navController.navigate(Routes.chatWithUser(sellerUid, sellerEmail, item.id, item.title))
                            },
                            onAddToWishlist = { navController.navigate(Routes.addItemToWishlist(item.id)) },
                            onDelete = {
                                // 开发者模式可以删除
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




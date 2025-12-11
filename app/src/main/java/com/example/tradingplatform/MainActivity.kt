package com.example.tradingplatform

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradingplatform.data.auth.TestUserSeeder
import com.example.tradingplatform.data.items.SampleItemSeeder
import com.example.tradingplatform.data.timetable.TimetableInitializer
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppLanguageState
import com.example.tradingplatform.ui.i18n.LocalAppStrings
import com.example.tradingplatform.ui.i18n.enStrings
import com.example.tradingplatform.ui.i18n.zhStrings
import com.example.tradingplatform.ui.navigation.AppNavHost
import com.example.tradingplatform.ui.theme.TradingPlatformTheme
import com.example.tradingplatform.ui.viewmodel.AchievementViewModel
import com.example.tradingplatform.ui.viewmodel.WishlistViewModel

class MainActivity : ComponentActivity() {
    
    // 通知权限请求 / Notification permission request
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "通知权限已授予")
        } else {
            android.util.Log.w("MainActivity", "通知权限被拒绝")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 请求通知权限（Android 13+）/ Request notification permission (Android 13+)
        requestNotificationPermission()
        setContent {
            TradingPlatformTheme {
                val languageState = remember { mutableStateOf(AppLanguage.ZH) }

                CompositionLocalProvider(
                    LocalAppLanguage provides languageState.value,
                    LocalAppLanguageState provides languageState,
                    LocalAppStrings provides when (languageState.value) {
                        AppLanguage.ZH -> zhStrings
                        AppLanguage.EN -> enStrings
                    }
                ) {
                    // 价格提醒检查 / Price alert check
                    val wishlistViewModel: WishlistViewModel = viewModel()
                    // 成就检查 / Achievement check
                    val achievementViewModel: AchievementViewModel = viewModel()
                    val context = LocalContext.current
                    
                    // Snackbar 状态 / Snackbar state
                    val snackbarHostState = remember { SnackbarHostState() }
                    val priceAlertMessage by wishlistViewModel.priceAlertMessage.collectAsState()
                    
                    // 显示降价提醒浮窗 / Show price drop alert snackbar
                    LaunchedEffect(priceAlertMessage) {
                        priceAlertMessage?.let { message ->
                            snackbarHostState.showSnackbar(
                                message = message,
                                withDismissAction = true
                            )
                            wishlistViewModel.clearPriceAlertMessage()
                        }
                    }
                    
                    LaunchedEffect(Unit) {
                        // 初始化测试用户（如果不存在则创建）/ Initialize test users (create if not exist)
                        TestUserSeeder.ensureTestUsers(context)
                        // 初始化课表课程数据（仅在首次运行时从 assets 导入）/ Initialize timetable course data (import from assets only on first run)
                        TimetableInitializer.ensureInitialized(context)
                        // 初始化示例商品数据（仅在本地 items 表为空时插入）/ Initialize sample item data (insert only when local items table is empty)
                        SampleItemSeeder.ensureSampleItems(context)
                        // 注意：价格提醒检查移到登录后执行 / Note: Price alert check moved to after login
                        // 检查成就 / Check achievements
                        achievementViewModel.checkAchievements()
                    }

                    // 使用 Scaffold 包裹以显示 Snackbar / Use Scaffold to show Snackbar
                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState) { data ->
                                Snackbar(
                                    snackbarData = data,
                                    containerColor = Color(0xFF4CAF50), // 绿色背景 / Green background
                                    contentColor = Color.White,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            AppNavHost()
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 请求通知权限（Android 13+）/ Request notification permission (Android 13+)
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 权限已授予 / Permission already granted
                    android.util.Log.d("MainActivity", "通知权限已存在")
                }
                else -> {
                    // 请求权限 / Request permission
                    android.util.Log.d("MainActivity", "请求通知权限...")
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

package com.example.tradingplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradingplatform.data.items.SampleItemSeeder
import com.example.tradingplatform.data.auth.AuthRepository
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                    val context = LocalContext.current

                    LaunchedEffect(Unit) {
                        val authRepo = AuthRepository(context)
                        when (authRepo.getPreferredLanguage()) {
                            "EN" -> languageState.value = AppLanguage.EN
                            "ZH" -> languageState.value = AppLanguage.ZH
                        }
                    }

                    LaunchedEffect(languageState.value) {
                        val authRepo = AuthRepository(context)
                        val code = if (languageState.value == AppLanguage.EN) "EN" else "ZH"
                        authRepo.setPreferredLanguage(code)
                    }

                    // 价格提醒检查
                    val wishlistViewModel: WishlistViewModel = viewModel()
                    // 成就检查
                    val achievementViewModel: AchievementViewModel = viewModel()
                    LaunchedEffect(Unit) {
                        // 初始化课表课程数据（仅在首次运行时从 assets 导入）
                        TimetableInitializer.ensureInitialized(context)
                        // 初始化示例商品数据（仅在本地 items 表为空时插入）
                        SampleItemSeeder.ensureSampleItems(context)
                        // 应用启动时检查一次价格
                        wishlistViewModel.checkPriceAlerts()
                        // 检查成就
                        achievementViewModel.checkAchievements()
                    }

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        AppNavHost(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

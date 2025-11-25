package com.example.tradingplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
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
                // 价格提醒检查
                val wishlistViewModel: WishlistViewModel = viewModel()
                // 成就检查
                val achievementViewModel: AchievementViewModel = viewModel()
                LaunchedEffect(Unit) {
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

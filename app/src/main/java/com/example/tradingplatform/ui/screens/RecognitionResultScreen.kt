package com.example.tradingplatform.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.data.vision.RecognitionResult
import com.example.tradingplatform.data.vision.RecommendedProduct
import com.example.tradingplatform.ui.i18n.LocalAppLanguage
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppStrings
import com.example.tradingplatform.ui.viewmodel.ImageRecognitionViewModel

@Composable
fun RecognitionResultScreen(
    capturedImage: Bitmap,
    onBack: () -> Unit,
    onItemClick: (Item) -> Unit,
    viewModel: ImageRecognitionViewModel = viewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val strings = LocalAppStrings.current

    LaunchedEffect(capturedImage) {
        viewModel.recognizeAndRecommend(capturedImage)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 顶部按钮 / Top button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.recognitionTitle,
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onBack) {
                Text(strings.myBack)
            }
        }

        when (val state = uiState) {
            is com.example.tradingplatform.ui.viewmodel.RecognitionUiState.Idle,
            is com.example.tradingplatform.ui.viewmodel.RecognitionUiState.Recognizing -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(strings.recognitionLoadingText)
                    }
                }
            }
            is com.example.tradingplatform.ui.viewmodel.RecognitionUiState.Success -> {
                // 显示拍摄的图片 / Display captured image
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Image(
                        bitmap = capturedImage.asImageBitmap(),
                        contentDescription = strings.recognitionCapturedImageContentDescription,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // 识别结果 / Recognition results
                if (state.recognitionResults.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = strings.recognitionResultSectionTitle,
                                style = MaterialTheme.typography.titleLarge
                            )
                            state.recognitionResults.take(3).forEach { result ->
                                RecognitionItem(result)
                            }
                        }
                    }
                }

                // 推荐商品 / Recommended products
                if (state.recommendedProducts.isNotEmpty()) {
                    Text(
                        text = strings.recognitionRecommendedTitle,
                        style = MaterialTheme.typography.titleLarge
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.recommendedProducts) { recommended ->
                            RecommendedProductCard(
                                recommended = recommended,
                                onClick = { onItemClick(recommended.item) }
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(strings.recognitionNoRecommendations)
                        }
                    }
                }
            }
            is com.example.tradingplatform.ui.viewmodel.RecognitionUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBack) {
                            Text(strings.myBack)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecognitionItem(result: RecognitionResult) {
    val lang = LocalAppLanguage.current
    val isEnglish = lang == AppLanguage.EN
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isEnglish) result.label else result.toChineseKeyword(),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (isEnglish) {
                    // 英文界面显示英文类别映射 / English interface shows English category mapping
                    when {
                        result.label.lowercase().contains("headphone") || 
                        result.label.lowercase().contains("earphone") ||
                        result.label.lowercase().contains("laptop") ||
                        result.label.lowercase().contains("computer") ||
                        result.label.lowercase().contains("phone") -> "Electronics"
                        result.label.lowercase().contains("book") -> "Books & Stationery"
                        result.label.lowercase().contains("clothing") ||
                        result.label.lowercase().contains("clothes") ||
                        result.label.lowercase().contains("shirt") ||
                        result.label.lowercase().contains("pants") ||
                        result.label.lowercase().contains("shoe") ||
                        result.label.lowercase().contains("glasses") -> "Clothing & Accessories"
                        result.label.lowercase().contains("furniture") ||
                        result.label.lowercase().contains("chair") ||
                        result.label.lowercase().contains("table") ||
                        result.label.lowercase().contains("sofa") ||
                        result.label.lowercase().contains("bed") ||
                        result.label.lowercase().contains("lamp") -> "Furniture & Appliances"
                        result.label.lowercase().contains("sports") ||
                        result.label.lowercase().contains("bicycle") ||
                        result.label.lowercase().contains("bike") -> "Sports & Fitness"
                        result.label.lowercase().contains("cosmetics") ||
                        result.label.lowercase().contains("makeup") -> "Beauty & Skincare"
                        result.label.lowercase().contains("food") ||
                        result.label.lowercase().contains("drink") -> "Food & Beverages"
                        result.label.lowercase().contains("toy") -> "Toys & Models"
                        result.label.lowercase().contains("car") ||
                        result.label.lowercase().contains("vehicle") -> "Automotive"
                        else -> "Other"
                    }
                } else {
                    result.toCategory()
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${(result.confidence * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun RecommendedProductCard(
    recommended: RecommendedProduct,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 商品图片 / Item image
            if (recommended.item.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(recommended.item.imageUrl),
                    contentDescription = recommended.item.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(strings.recognitionNoImageLabel)
                }
            }

            // 商品信息 / Item information
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = recommended.item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "¥${String.format("%.2f", recommended.item.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // 匹配原因 / Match reasons
                if (recommended.matchReasons.isNotEmpty()) {
                    recommended.matchReasons.take(2).forEach { reason ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = reason,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // 匹配分数 / Match score
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${recommended.score.toInt()}${strings.recognitionScoreSuffix}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


package com.example.tradingplatform.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.data.vision.ImageRecognitionService
import com.example.tradingplatform.data.vision.ProductRecommendationService
import com.example.tradingplatform.data.vision.RecognitionResult
import com.example.tradingplatform.data.vision.RecommendedProduct
import com.example.tradingplatform.data.vision.ThirdPartyRecognitionService
import com.example.tradingplatform.data.vision.ApiConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface RecognitionUiState {
    data object Idle : RecognitionUiState
    data object Recognizing : RecognitionUiState
    data class Success(
        val recognitionResults: List<RecognitionResult>,
        val recommendedProducts: List<RecommendedProduct>
    ) : RecognitionUiState
    data class Error(val message: String) : RecognitionUiState
}

enum class RecognitionType {
    ML_KIT_DEVICE,      // ML Kit 设备端
    ML_KIT_CLOUD,       // ML Kit 云端
    THIRD_PARTY_ALIYUN, // 阿里云
    THIRD_PARTY_TENCENT,// 腾讯云
    THIRD_PARTY_BAIDU   // 百度 AI
}

class ImageRecognitionViewModel(
    application: Application,
    recognitionType: RecognitionType = RecognitionType.ML_KIT_DEVICE
) : AndroidViewModel(application) {
    private val recognitionService = when (recognitionType) {
        RecognitionType.ML_KIT_DEVICE -> ImageRecognitionService(application, false)
        RecognitionType.ML_KIT_CLOUD -> ImageRecognitionService(application, true)
        RecognitionType.THIRD_PARTY_ALIYUN -> null // 使用第三方服务
        RecognitionType.THIRD_PARTY_TENCENT -> null
        RecognitionType.THIRD_PARTY_BAIDU -> null
    }
    
    private val thirdPartyService = when (recognitionType) {
        RecognitionType.THIRD_PARTY_ALIYUN -> ThirdPartyRecognitionService(ThirdPartyRecognitionService.ApiType.ALIYUN)
        RecognitionType.THIRD_PARTY_TENCENT -> ThirdPartyRecognitionService(ThirdPartyRecognitionService.ApiType.TENCENT)
        RecognitionType.THIRD_PARTY_BAIDU -> ThirdPartyRecognitionService(ThirdPartyRecognitionService.ApiType.BAIDU)
        else -> null
    }
    
    private val recommendationService = ProductRecommendationService(application)

    private val _state = MutableStateFlow<RecognitionUiState>(RecognitionUiState.Idle)
    val state: StateFlow<RecognitionUiState> = _state

    /**
     * 识别图片并推荐商品
     */
    fun recognizeAndRecommend(bitmap: Bitmap) {
        _state.value = RecognitionUiState.Recognizing
        viewModelScope.launch {
            try {
                val recognitionResults: List<RecognitionResult>
                
                // 根据识别类型选择服务
                if (thirdPartyService != null) {
                    // 使用第三方 API
                    val thirdPartyResults = thirdPartyService.recognizeProduct(bitmap)
                    recognitionResults = thirdPartyResults.map { it.toRecognitionResult() }
                } else if (recognitionService != null) {
                    // 使用 ML Kit
                    recognitionResults = recognitionService.recognizeImage(bitmap)
                } else {
                    _state.value = RecognitionUiState.Error("识别服务未配置")
                    return@launch
                }
                
                if (recognitionResults.isEmpty()) {
                    _state.value = RecognitionUiState.Error("未能识别出物体，请重新拍照")
                    return@launch
                }

                // 推荐商品
                val recommendedProducts = recommendationService.recommendProducts(recognitionResults)

                _state.value = RecognitionUiState.Success(recognitionResults, recommendedProducts)
            } catch (e: Exception) {
                Log.e("ImageRecognitionViewModel", "识别失败", e)
                _state.value = RecognitionUiState.Error("识别失败: ${e.message ?: "未知错误"}")
            }
        }
    }

    /**
     * 重置状态
     */
    fun reset() {
        _state.value = RecognitionUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        recognitionService?.close()
    }
}


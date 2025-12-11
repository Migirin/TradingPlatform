package com.example.tradingplatform.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import java.io.ByteArrayOutputStream
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradingplatform.ui.i18n.LocalAppLanguage
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppStrings
import com.example.tradingplatform.ui.viewmodel.ImageRecognitionViewModel
import com.example.tradingplatform.ui.viewmodel.RecognitionType
import androidx.camera.core.ImageProxy
import androidx.camera.core.ImageCaptureException
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onResult: (Bitmap) -> Unit,
    recognitionType: RecognitionType = RecognitionType.ML_KIT_DEVICE
) {
    val context = LocalContext.current
    val viewModel: ImageRecognitionViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ImageRecognitionViewModel(
                    context.applicationContext as android.app.Application,
                    recognitionType
                ) as T
            }
        }
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    val strings = LocalAppStrings.current
    var hasPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    ) }

    // 权限请求启动器 / Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    // 首次进入时自动请求权限 / Auto request permission on first entry
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasPermission) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(strings.cameraPermissionRequired)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text(strings.cameraGrantPermission)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onBack) {
                Text(strings.myBack)
            }
        }
        return
    }

    CameraPreview(
        onImageCaptured = { bitmap ->
            onResult(bitmap)
        },
        onBack = onBack
    )
}

@Composable
fun CameraPreview(
    onImageCaptured: (Bitmap) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val strings = LocalAppStrings.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var preview: Preview? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var errorMessage: String? by remember { mutableStateOf(null) }

    // 初始化相机提供者 / Initialize camera provider
    LaunchedEffect(Unit) {
        try {
            val future = ProcessCameraProvider.getInstance(context)
            val provider = suspendCancellableCoroutine<ProcessCameraProvider> { continuation ->
                future.addListener({
                    try {
                        continuation.resume(future.get())
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
            cameraProvider = provider
            android.util.Log.d("CameraPreview", "相机提供者初始化成功")
        } catch (e: Exception) {
            android.util.Log.e("CameraPreview", "获取相机提供者失败", e)
            errorMessage = java.lang.String.format(strings.cameraInitError, e.message ?: "")
        }
    }

    // 清理资源 / Cleanup resources
    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            android.util.Log.d("CameraPreview", "相机资源已清理")
        }
    }

    // 预览视图引用 / Preview view reference
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView = this
                    android.util.Log.d("CameraPreview", "PreviewView 已创建")
                }
            },
            modifier = Modifier.fillMaxSize(),
            onRelease = {
                // 释放时取消绑定 / Unbind on release
                cameraProvider?.unbindAll()
                preview = null
                imageCapture = null
                previewView = null
                android.util.Log.d("CameraPreview", "PreviewView 已释放")
            }
        )
        
        // 如果预览没有画面（模拟器常见问题），显示提示
        if (preview != null && cameraProvider != null && previewView != null) {
            LaunchedEffect(preview, cameraProvider) {
                kotlinx.coroutines.delay(2000) // 等待2秒
                // 检查预览是否真的在显示（模拟器可能无法提供画面）
                previewView?.let { view ->
                    if (view.width > 0 && view.height > 0) {
                        android.util.Log.d("CameraPreview", "PreviewView 尺寸: ${view.width}x${view.height}")
                        // 模拟器可能无法提供画面，但功能仍然可用（可以拍照）
                    }
                }
            }
        }
    }

    // 当相机提供者准备好后，绑定相机 / Bind camera when camera provider is ready
    LaunchedEffect(cameraProvider, previewView) {
        if (cameraProvider != null && previewView != null && preview == null) {
            try {
                android.util.Log.d("CameraPreview", "开始绑定相机")
                
                val provider = cameraProvider!!
                val view = previewView!!
                
                // 取消之前的绑定 / Unbind previous bindings
                provider.unbindAll()

                // 创建预览 / Create preview
                val newPreview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(view.surfaceProvider)
                        android.util.Log.d("CameraPreview", "预览 SurfaceProvider 已设置")
                    }
                preview = newPreview
                android.util.Log.d("CameraPreview", "预览对象已创建，等待绑定")

                // 创建图像捕获 / Create image capture
                val newImageCapture = ImageCapture.Builder()
                    .setTargetRotation(view.display.rotation)
                    .build()
                imageCapture = newImageCapture

                // 检查可用的摄像头 / Check available cameras
                val cameraInfo = provider.availableCameraInfos
                android.util.Log.d("CameraPreview", "可用摄像头数量: ${cameraInfo.size}")
                
                var bindSuccess = false
                var lastException: Exception? = null
                var cameraSelector: CameraSelector? = null
                
                // 策略1：优先尝试后置摄像头 / Strategy 1: Try back camera first
                try {
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        newPreview,
                        newImageCapture
                    )
                    android.util.Log.d("CameraPreview", "后置摄像头绑定成功")
                    bindSuccess = true
                } catch (e: Exception) {
                    android.util.Log.w("CameraPreview", "后置摄像头绑定失败，尝试前置摄像头", e)
                    lastException = e
                    provider.unbindAll()
                    
                    // 策略2：尝试前置摄像头 / Strategy 2: Try front camera
                    try {
                        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            newPreview,
                            newImageCapture
                        )
                        android.util.Log.d("CameraPreview", "前置摄像头绑定成功")
                        bindSuccess = true
                    } catch (e2: Exception) {
                        android.util.Log.w("CameraPreview", "前置摄像头也绑定失败，尝试使用第一个可用相机", e2)
                        lastException = e2
                        provider.unbindAll()
                        
                        // 策略3：模拟器相机可能没有 lensFacing 信息，尝试使用第一个可用相机 / Strategy 3: Use first available camera (for emulators)
                        if (cameraInfo.isNotEmpty()) {
                            try {
                                // 获取相机管理器
                                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
                                val cameraIds = cameraManager.cameraIdList
                                
                                android.util.Log.d("CameraPreview", "找到 ${cameraIds.size} 个相机ID: ${cameraIds.joinToString()}")
                                
                                if (cameraIds.isNotEmpty()) {
                                    // 创建一个接受所有相机的选择器（不限制 lensFacing）
                                    val anyCameraSelector = CameraSelector.Builder()
                                        .addCameraFilter { cameras ->
                                            // 接受所有可用的相机
                                            cameras
                                        }
                                        .build()
                                    
                                    provider.bindToLifecycle(
                                        lifecycleOwner,
                                        anyCameraSelector,
                                        newPreview,
                                        newImageCapture
                                    )
                                    android.util.Log.d("CameraPreview", "使用第一个可用相机绑定成功 (相机ID: ${cameraIds[0]})")
                                    bindSuccess = true
                                    cameraSelector = anyCameraSelector
                                }
                            } catch (e3: Exception) {
                                android.util.Log.e("CameraPreview", "使用第一个可用相机失败: ${e3.message}", e3)
                                lastException = e3
                            }
                        }
                    }
                }
                
                if (bindSuccess) {
                    android.util.Log.d("CameraPreview", "相机绑定成功")
                    android.util.Log.d("CameraPreview", "PreviewView 状态: ${view.width}x${view.height}, visibility=${view.visibility}")
                    
                    // 检查预览是否真的在运行（模拟器可能无法提供画面）
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        android.util.Log.d("CameraPreview", "延迟检查：PreviewView 是否显示画面")
                        if (view.width > 0 && view.height > 0) {
                            android.util.Log.d("CameraPreview", "PreviewView 尺寸正常，但可能模拟器无法提供画面数据")
                        }
                    }, 1000)
                    
                    errorMessage = null
                } else {
                    val errorMsg = lastException?.message ?: "无法初始化相机"
                    android.util.Log.e("CameraPreview", "所有摄像头绑定失败: $errorMsg")
                    errorMessage = "相机初始化失败：$errorMsg\n\n" +
                            "这是模拟器相机配置问题。\n\n" +
                            "解决方案：\n" +
                            "1. AVD Manager -> Edit -> Advanced Settings -> Camera\n" +
                            "2. 设置 Front Camera 和 Back Camera 为 'Webcam0' 或 'Emulated'\n" +
                            "3. 重启模拟器\n" +
                            "4. 或使用真实设备测试（推荐）"
                }
            } catch (e: Exception) {
                android.util.Log.e("CameraPreview", "相机初始化失败", e)
                errorMessage = java.lang.String.format(
                    strings.cameraInitFailed + "\n\n错误详情: %s\n\n" +
                    "解决方案：\n" +
                    "1. 检查模拟器相机配置（AVD Manager -> Edit -> Advanced Settings）\n" +
                    "2. 确保启用了 Front Camera 和 Back Camera\n" +
                    "3. 或使用真实设备测试",
                    e.message ?: "未知错误"
                )
            }
        }
    }

    // 显示错误信息 / Display error message
    errorMessage?.let { error ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = onBack) {
                    Text(strings.myBack)
                }
            }
        }
    }

    // 拍照按钮和返回按钮 / Capture button and back button
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack) {
                Text(strings.cameraCancel)
            }
            
            FloatingActionButton(
                onClick = {
                    imageCapture?.let { capture ->
                        try {
                            captureImage(capture, onImageCaptured)
                        } catch (e: Exception) {
                            android.util.Log.e("CameraScreen", "拍照按钮点击失败", e)
                        }
                    } ?: run {
                        android.util.Log.w("CameraScreen", "ImageCapture 未初始化")
                    }
                },
                modifier = Modifier.size(64.dp)
            ) {
                Text("📷", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

private fun captureImage(
    imageCapture: ImageCapture,
    onImageCaptured: (Bitmap) -> Unit
) {
    val executor = Executors.newSingleThreadExecutor()
    
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(imageProxy: ImageProxy) {
            // 在后台线程处理图片 / Process image in background thread
            executor.execute {
                try {
                    val bitmap = imageProxyToBitmap(imageProxy)
                    val rotation = imageProxy.imageInfo.rotationDegrees.toFloat()
                    imageProxy.close()
                    
                    val rotatedBitmap = rotateBitmap(bitmap, rotation)
                    
                    // 切换到主线程调用回调 / Switch to main thread to call callback
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onImageCaptured(rotatedBitmap)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CameraScreen", "图片处理失败", e)
                    imageProxy.close()
                }
            }
        }

        override fun onError(exception: ImageCaptureException) {
            android.util.Log.e("CameraScreen", "拍照失败", exception)
            exception.printStackTrace()
        }
    })
}

private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
    val format = imageProxy.format
    android.util.Log.d("CameraScreen", "ImageProxy format: $format, planes: ${imageProxy.planes.size}")
    
    // 如果是 JPEG 格式，直接解码 / If JPEG format, decode directly
    if (format == ImageFormat.JPEG) {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IllegalStateException("无法解码 JPEG 图片")
    }
    
    // 如果是 YUV_420_888 格式，转换为 Bitmap / If YUV_420_888 format, convert to Bitmap
    if (format == ImageFormat.YUV_420_888) {
        if (imageProxy.planes.size < 3) {
            throw IllegalStateException("YUV 格式需要至少 3 个平面")
        }
        
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        val nv21 = ByteArray(ySize + uSize + vSize)
        
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        
        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            imageProxy.width,
            imageProxy.height,
            null
        )
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            Rect(0, 0, imageProxy.width, imageProxy.height),
            100,
            out
        )
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: throw IllegalStateException("无法解码 YUV 图片")
    }
    
    throw UnsupportedOperationException("不支持的图片格式: $format")
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}


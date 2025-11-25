# 第三方商品识别 API 配置指南

## 概述

除了 Firebase ML Kit，还可以使用第三方云服务的商品识别 API，这些服务通常提供更专业的商品识别能力。

## 支持的第三方服务

### 1. 阿里云商品识别

**优势**：
- 识别准确率高
- 支持中文商品识别
- 可以识别品牌、型号、价格

**配置步骤**：
1. 注册阿里云账号
2. 开通"商品识别"服务
3. 获取 AccessKeyId 和 AccessKeySecret
4. 在代码中配置密钥

**API 文档**：https://help.aliyun.com/document_detail/175144.html

### 2. 腾讯云商品识别

**优势**：
- 识别速度快
- 支持多种商品类别
- 提供相似商品推荐

**配置步骤**：
1. 注册腾讯云账号
2. 开通"商品识别"服务
3. 获取 SecretId 和 SecretKey
4. 在代码中配置密钥

**API 文档**：https://cloud.tencent.com/document/product/865/35495

### 3. 百度 AI 商品识别

**优势**：
- 免费额度较高
- 识别准确率不错
- 支持多种识别场景

**配置步骤**：
1. 注册百度智能云账号
2. 创建应用
3. 获取 API Key 和 Secret Key
4. 在代码中配置密钥

**API 文档**：https://ai.baidu.com/ai-doc/IMAGERECOGNITION/8k3h7xr6v

## 实现示例

### 阿里云商品识别实现

```kotlin
// 1. 添加依赖
implementation("com.aliyun:aliyun-java-sdk-core:4.6.3")

// 2. 实现 API 调用
class AliyunRecognitionService {
    private val accessKeyId = "your-access-key-id"
    private val accessKeySecret = "your-access-key-secret"
    
    suspend fun recognizeProduct(imageBase64: String): ProductRecognitionResult {
        // 使用阿里云 SDK 调用 API
        // 返回识别结果
    }
}
```

### 腾讯云商品识别实现

```kotlin
// 1. 添加依赖
implementation("com.tencentcloudapi:tencentcloud-sdk-java:3.1.0")

// 2. 实现 API 调用
class TencentRecognitionService {
    private val secretId = "your-secret-id"
    private val secretKey = "your-secret-key"
    
    suspend fun recognizeProduct(imageBase64: String): ProductRecognitionResult {
        // 使用腾讯云 SDK 调用 API
        // 返回识别结果
    }
}
```

## 配置密钥

### 方式1：使用 SupabaseConfig（推荐）

在 `SupabaseConfig.kt` 中添加：

```kotlin
object SupabaseConfig {
    // ... 现有配置
    
    // 第三方 API 配置
    object ThirdPartyAPI {
        // 阿里云
        const val ALIYUN_ACCESS_KEY_ID = "your-key-id"
        const val ALIYUN_ACCESS_KEY_SECRET = "your-key-secret"
        
        // 腾讯云
        const val TENCENT_SECRET_ID = "your-secret-id"
        const val TENCENT_SECRET_KEY = "your-secret-key"
        
        // 百度 AI
        const val BAIDU_API_KEY = "your-api-key"
        const val BAIDU_SECRET_KEY = "your-secret-key"
    }
}
```

### 方式2：使用环境变量

在 `local.properties` 中添加（不要提交到 Git）：

```properties
aliyun.access.key.id=your-key-id
aliyun.access.key.secret=your-key-secret
```

## 使用方式

在 `ImageRecognitionViewModel` 中切换识别服务：

```kotlin
class ImageRecognitionViewModel(
    application: Application,
    recognitionType: RecognitionType = RecognitionType.ML_KIT_DEVICE
) : AndroidViewModel(application) {
    enum class RecognitionType {
        ML_KIT_DEVICE,      // ML Kit 设备端
        ML_KIT_CLOUD,       // ML Kit 云端
        THIRD_PARTY_ALIYUN, // 阿里云
        THIRD_PARTY_TENCENT,// 腾讯云
        THIRD_PARTY_BAIDU   // 百度 AI
    }
    
    private val recognitionService = when (recognitionType) {
        RecognitionType.ML_KIT_DEVICE -> ImageRecognitionService(application, false)
        RecognitionType.ML_KIT_CLOUD -> ImageRecognitionService(application, true)
        RecognitionType.THIRD_PARTY_ALIYUN -> ThirdPartyRecognitionService(ApiType.ALIYUN)
        // ... 其他类型
    }
}
```

## 费用对比

| 服务 | 免费额度 | 付费价格 |
|------|---------|---------|
| ML Kit 设备端 | 无限 | 免费 |
| ML Kit 云端 | 有限 | 按使用量 |
| 阿里云 | 有限 | 按调用次数 |
| 腾讯云 | 有限 | 按调用次数 |
| 百度 AI | 较高 | 按调用次数 |

## 推荐方案

1. **开发/测试阶段**：使用 ML Kit 设备端模型（免费、快速）
2. **生产环境（低流量）**：使用 ML Kit 云端模型（准确性高、配置简单）
3. **生产环境（高流量）**：使用第三方 API（更专业的商品识别）

## 注意事项

1. **API 密钥安全**：不要将密钥提交到 Git，使用环境变量或配置文件
2. **费用控制**：设置 API 调用限制，避免意外费用
3. **错误处理**：实现重试机制和降级策略（云端失败时使用设备端）
4. **隐私政策**：使用第三方 API 时，需要在隐私政策中说明图片上传



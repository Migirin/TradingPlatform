# Firebase 云端模型配置指南

## 概述

要使用 ML Kit 的云端模型进行商品识别，需要配置 Firebase 项目。云端模型提供更高的识别准确性和更丰富的识别结果。

## 配置步骤

### 1. 创建 Firebase 项目

1. 访问 [Firebase Console](https://console.firebase.google.com/)
2. 点击"添加项目"
3. 输入项目名称
4. 按照向导完成项目创建

### 2. 添加 Android 应用

1. 在 Firebase Console 中，点击"添加应用" → Android
2. 输入 Android 包名（如：`com.cao.secondhand`）
3. 下载 `google-services.json` 文件
4. 将文件放到 `app/` 目录下

### 3. 配置 Gradle

#### 项目级 `build.gradle.kts`
```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}
```

#### 应用级 `app/build.gradle.kts`
```kotlin
plugins {
    // ... 其他插件
    id("com.google.gms.google-services")
}

dependencies {
    // ML Kit 云端模型
    implementation("com.google.mlkit:image-labeling-custom:17.0.2")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
}
```

### 4. 启用 ML Kit API

1. 在 Firebase Console 中，进入"构建" → "ML Kit"
2. 启用"图像标签"API
3. 如果需要，启用"自定义模型"功能

### 5. 配置 API 密钥

1. 在 Firebase Console 中，进入"项目设置" → "服务账号"
2. 生成新的私钥（JSON 文件）
3. 在应用中配置 API 密钥（可选，通常自动处理）

### 6. 更新代码

代码已经支持云端模型，只需要在创建 `ImageRecognitionViewModel` 时传入 `useCloudModel = true`：

```kotlin
val viewModel: ImageRecognitionViewModel = viewModel(
    factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ImageRecognitionViewModel(application, useCloudModel = true) as T
        }
    }
)
```

## 云端模型 vs 设备端模型

| 特性 | 设备端模型 | 云端模型 |
|------|-----------|---------|
| 网络要求 | 无需网络 | 需要网络 |
| 响应速度 | 快速（本地） | 较慢（需上传） |
| 准确性 | 中等 | 高 |
| 识别范围 | 常见物体 | 更丰富的类别 |
| 成本 | 免费 | 可能有费用 |
| 隐私 | 图片不上传 | 图片上传到服务器 |

## 注意事项

1. **费用**：云端模型可能有使用费用，请查看 Firebase 定价
2. **配额**：注意 API 调用配额限制
3. **网络**：确保设备有稳定的网络连接
4. **隐私**：图片会上传到 Google 服务器，注意隐私政策

## 测试

配置完成后，测试云端识别：

1. 确保设备连接到网络
2. 使用拍照识别功能
3. 查看日志，应该看到"云端识别成功"的日志
4. 识别结果应该更准确和详细

## 故障排除

### 问题：识别失败，提示需要网络
- 检查设备网络连接
- 检查 Firebase 配置是否正确
- 检查 API 是否已启用

### 问题：识别结果与设备端相同
- 确认 `useCloudModel = true`
- 检查网络连接
- 查看日志确认使用的是云端模型

### 问题：应用崩溃
- 检查 `google-services.json` 是否正确放置
- 检查 Gradle 配置是否正确
- 查看错误日志




# 第三方 API 使用指南

## 快速开始

### 1. 配置 API 密钥

#### 方式1：使用 local.properties（推荐）

1. 复制 `local.properties.example` 为 `local.properties`
2. 在 `local.properties` 中填入你的 API 密钥：

```properties
# 百度 AI 配置（推荐，免费额度高）
baidu.api.key=your-baidu-api-key
baidu.secret.key=your-baidu-secret-key

# 阿里云配置
aliyun.access.key.id=your-aliyun-access-key-id
aliyun.access.key.secret=your-aliyun-access-key-secret

# 腾讯云配置
tencent.secret.id=your-tencent-secret-id
tencent.secret.key=your-tencent-secret-key
```

3. **重要**：确保 `local.properties` 在 `.gitignore` 中，不要提交到 Git

#### 方式2：直接修改 ApiConfig.kt（仅用于测试）

在 `ApiConfig.kt` 中直接填入密钥（不推荐用于生产环境）

### 2. 获取 API 密钥

#### 百度 AI（推荐）

1. 访问 [百度智能云](https://cloud.baidu.com/)
2. 注册/登录账号
3. 进入"产品服务" → "人工智能" → "图像识别"
4. 创建应用，获取 API Key 和 Secret Key
5. **免费额度**：每天 50000 次调用

**优势**：
- 免费额度高
- 配置简单
- 识别准确率不错
- 支持中文商品识别

#### 阿里云

1. 访问 [阿里云](https://www.aliyun.com/)
2. 注册/登录账号
3. 开通"图像识别"服务
4. 在"访问控制"中创建 AccessKey
5. 获取 AccessKeyId 和 AccessKeySecret

**优势**：
- 识别准确率高
- 支持商品识别
- 可以识别品牌、型号

#### 腾讯云

1. 访问 [腾讯云](https://cloud.tencent.com/)
2. 注册/登录账号
3. 开通"商品识别"服务
4. 在"访问管理"中创建密钥
5. 获取 SecretId 和 SecretKey

**优势**：
- 识别速度快
- 支持相似商品推荐

### 3. 使用第三方 API

1. **配置密钥**：按照上述步骤配置 API 密钥

2. **选择识别方式**：
   - 打开拍照界面
   - 选择"百度AI"、"阿里云"或"腾讯云"按钮
   - 如果按钮是灰色，说明该 API 未配置

3. **拍照识别**：
   - 点击拍照按钮
   - 系统会自动调用第三方 API 进行识别
   - 显示识别结果和推荐商品

## 功能对比

| 特性 | 百度 AI | 阿里云 | 腾讯云 |
|------|---------|--------|--------|
| **免费额度** | 每天 50000 次 | 有限 | 有限 |
| **配置难度** | ⭐ 简单 | ⭐⭐ 中等 | ⭐⭐ 中等 |
| **识别准确率** | ⭐⭐⭐ 高 | ⭐⭐⭐⭐ 很高 | ⭐⭐⭐ 高 |
| **识别速度** | ⭐⭐⭐ 快 | ⭐⭐⭐ 快 | ⭐⭐⭐⭐ 很快 |
| **中文支持** | ✅ 优秀 | ✅ 良好 | ✅ 良好 |
| **商品识别** | ✅ 支持 | ✅ 支持 | ✅ 支持 |
| **推荐使用** | ✅ 推荐 | 高准确率需求 | 高速度需求 |

## 当前实现状态

### ✅ 已实现

- **百度 AI**：完整实现
  - Access Token 获取
  - 商品识别 API 调用
  - 响应解析
  - 结果转换

- **阿里云**：框架已实现
  - API 调用框架
  - 签名算法框架（需要完善）
  - 响应解析框架

- **腾讯云**：框架已实现
  - API 调用框架
  - 需要实现签名算法

### 🔧 需要完善

1. **阿里云签名算法**：
   - 当前签名算法是简化版
   - 需要按照 [阿里云文档](https://help.aliyun.com/document_detail/175144.html) 实现完整签名

2. **腾讯云签名算法**：
   - 需要实现腾讯云的签名算法
   - 参考 [腾讯云文档](https://cloud.tencent.com/document/product/865/35495)

3. **错误处理**：
   - 添加重试机制
   - 网络错误处理
   - API 限流处理

## 使用示例

### 百度 AI 完整示例

```kotlin
// 1. 配置密钥（在 local.properties 中）
baidu.api.key=your-api-key
baidu.secret.key=your-secret-key

// 2. 在拍照界面选择"百度AI"
// 3. 拍照后自动识别
```

### 测试 API 配置

在 `ApiConfig.kt` 中检查配置：

```kotlin
// 检查配置是否完整
if (ApiConfig.isBaiduConfigured()) {
    println("百度 AI 配置完整")
} else {
    println("百度 AI 配置不完整")
}
```

## 常见问题

### Q: 为什么按钮是灰色的？

A: 按钮灰色表示该 API 未配置。请检查：
1. `local.properties` 文件是否存在
2. API 密钥是否正确配置
3. 密钥格式是否正确（没有多余空格）

### Q: 识别失败怎么办？

A: 检查以下几点：
1. 网络连接是否正常
2. API 密钥是否正确
3. API 配额是否用完
4. 查看日志输出（Logcat）

### Q: 如何查看识别日志？

A: 在 Android Studio 的 Logcat 中过滤 `ThirdPartyRecognition` 标签

### Q: 哪个 API 最好？

A: 推荐使用**百度 AI**：
- 免费额度高
- 配置简单
- 识别准确率不错
- 适合开发测试

## 下一步

1. **完善阿里云实现**：实现完整的签名算法
2. **完善腾讯云实现**：实现签名算法
3. **添加错误处理**：网络重试、降级策略
4. **添加缓存**：缓存识别结果，减少 API 调用
5. **添加统计**：统计识别成功率、API 使用量

## 技术支持

- 百度 AI 文档：https://ai.baidu.com/ai-doc/IMAGERECOGNITION/8k3h7xr6v
- 阿里云文档：https://help.aliyun.com/document_detail/175144.html
- 腾讯云文档：https://cloud.tencent.com/document/product/865/35495




# 百度 AI 创建应用指南

## 步骤 1：注册/登录百度智能云

1. 访问 [百度智能云官网](https://cloud.baidu.com/)
2. 点击右上角"登录"或"注册"
3. 使用百度账号登录（如果没有账号，先注册）

## 步骤 2：进入控制台

1. 登录后，点击右上角的"控制台"按钮
2. 或者直接访问：https://console.bce.baidu.com/

## 步骤 3：开通图像识别服务

### 方式1：通过顶部导航栏（推荐）

1. 在控制台页面顶部，找到导航栏
2. 点击"产品"或"产品服务"（通常在"控制台总览"旁边）
3. 在下拉菜单中找到"人工智能" → "图像识别"
4. 或者直接访问：https://cloud.baidu.com/product/imageprocess
5. 点击"立即使用"或"开通服务"
6. 阅读并同意服务协议
7. 选择计费方式（推荐选择"按量付费"）

### 方式2：通过搜索框

1. 在控制台页面，找到搜索框（显示"搜索文档、API、产品介绍和解决方案"）
2. 输入"图像识别"或"image recognition"
3. 在搜索结果中点击"图像识别"产品
4. 进入产品页面后点击"立即使用"

### 方式3：通过左侧推荐区域

1. 在控制台左侧"为您推荐"区域
2. 找到"热门产品, 精选推荐"部分
3. 如果看到图像识别相关推荐，可以直接点击
4. 或者点击"查看更多"浏览所有产品

### 方式4：直接访问产品页面

直接访问图像识别产品页面：
- https://cloud.baidu.com/product/imageprocess
- 点击"立即使用"或"开通服务"

## 步骤 4：创建应用

### 方法1：在图像识别控制台创建

1. 开通服务后，进入"图像识别"控制台
2. 点击左侧菜单"应用列表"
3. 点击"创建应用"按钮
4. 填写应用信息：
   - **应用名称**：例如"商品识别应用"
   - **应用类型**：选择"通用物体和场景识别"或"商品识别"
   - **接口选择**：选择需要的接口（推荐选择"通用物体和场景识别"）
   - **应用描述**：可选，填写应用用途
5. 点击"立即创建"

### 方法2：通过 API 管理创建

1. 在控制台左侧菜单，找到"产品服务" → "人工智能" → "图像识别"
2. 点击"应用管理"或"我的应用"
3. 点击"创建应用"
4. 填写应用信息并创建

## 步骤 5：获取 API Key 和 Secret Key

创建应用成功后，你会看到应用列表，每个应用都有：

1. **API Key**：应用的唯一标识
2. **Secret Key**：应用的密钥（用于获取 Access Token）

### 查看密钥的方法：

1. 在应用列表中，找到你刚创建的应用
2. 点击应用名称或"查看详情"
3. 在应用详情页面，可以看到：
   - **API Key**：一串类似 `xxxxxxxxxxxxxxxxxxxx` 的字符串
   - **Secret Key**：一串类似 `xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx` 的字符串
4. **重要**：Secret Key 只显示一次，请妥善保存！

## 步骤 6：配置到项目中

### 方式1：使用 local.properties（推荐）

1. 在项目根目录创建 `local.properties` 文件（如果不存在）
2. 添加以下内容：

```properties
# 百度 AI 配置
baidu.api.key=你的API_Key
baidu.secret.key=你的Secret_Key
```

3. 将 `你的API_Key` 和 `你的Secret_Key` 替换为实际的值

### 方式2：直接修改 ApiConfig.kt（仅用于测试）

在 `app/src/main/java/com/example/tradingplatform/data/vision/ApiConfig.kt` 中：

```kotlin
object Baidu {
    val API_KEY: String = "你的API_Key"
    val SECRET_KEY: String = "你的Secret_Key"
}
```

## 步骤 7：验证配置

1. 运行应用
2. 进入拍照识别界面
3. 如果"百度AI"按钮可以点击（不是灰色），说明配置成功
4. 拍照测试识别功能

## 常见问题

### Q: 找不到"创建应用"按钮？

A: 确保你已经：
1. 开通了图像识别服务
2. 进入了正确的控制台页面
3. 如果还是找不到，尝试刷新页面

### Q: Secret Key 忘记了怎么办？

A: Secret Key 只显示一次，如果忘记了：
1. 删除旧应用
2. 重新创建新应用
3. 获取新的 API Key 和 Secret Key

### Q: 应用创建后多久可以使用？

A: 通常立即生效，可以马上使用

### Q: 免费额度是多少？

A: 百度 AI 图像识别提供：
- **每天 50000 次**免费调用
- 超出后按量付费（价格很低）

### Q: 如何查看调用量？

A: 在控制台的"用量统计"或"账单"中查看

## 安全提示

1. **不要将 API Key 和 Secret Key 提交到 Git**
2. 使用 `local.properties` 存储密钥（该文件已在 `.gitignore` 中）
3. 如果密钥泄露，立即在控制台删除应用并重新创建
4. 定期更换密钥（建议每 3-6 个月）

## 下一步

配置完成后：
1. 运行应用测试识别功能
2. 查看识别结果是否准确
3. 根据需要调整识别参数

## 相关链接

- [百度智能云控制台](https://console.bce.baidu.com/)
- [图像识别产品页面](https://cloud.baidu.com/product/imageprocess)
- [API 文档](https://ai.baidu.com/ai-doc/IMAGERECOGNITION/8k3h7xr6v)
- [定价说明](https://cloud.baidu.com/product/imageprocess.html#price)


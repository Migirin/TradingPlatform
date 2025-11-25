# 邮件服务配置说明

## 使用 SendGrid 发送验证邮件

### 1. 注册 SendGrid 账号

1. 访问 https://sendgrid.com/
2. 点击 "Start for free" 注册免费账号
3. 完成邮箱验证

### 2. 跳过域名设置（开发测试推荐）

**在当前的 "Set up Sending" 页面：**

1. 点击右上角的 **"Skip to dashboard"** 按钮
   - 域名验证需要 DNS 配置，开发测试可以跳过
   - 使用 "Single Sender Verification" 更简单快捷

### 3. 验证单个发件人邮箱（推荐用于开发）

1. 进入 SendGrid 控制台后，点击左侧菜单 **Settings** → **Sender Authentication**
2. 选择 **Verify a Single Sender**（单个邮箱验证）
3. 填写发件人信息：
   - **From Email Address**: 您的邮箱地址（如：`yourname@ucdconnect.ie` 或任何可用邮箱）
   - **From Name**: `BJUT SecondHand`
   - **Reply To**: 可以留空或填写相同邮箱
   - **Company Address**: 填写您的地址（必填）
   - **City**: 填写城市（必填）
   - **State**: 填写州/省（必填）
   - **Country**: 选择国家（必填）
   - **Zip Code**: 填写邮编（必填）
4. 勾选同意条款
5. 点击 **Create**
6. **重要**：检查您的邮箱（包括垃圾邮件文件夹），找到 SendGrid 发送的验证邮件
7. 点击邮件中的验证链接完成验证

### 4. 创建 API Key

1. 进入 **Settings** → **API Keys**
2. 点击 **Create API Key**
3. 输入 API Key 名称（如：`TradingPlatform`）
4. 选择权限：
   - **Full Access**（完整权限，开发期推荐）
   - 或 **Restricted Access** → 选择 **Mail Send** 权限（生产环境推荐）
5. 点击 **Create & View**
6. **重要**：立即复制 API Key（格式类似：`SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`）
   - ⚠️ 只显示一次，请妥善保存！
   - 如果丢失，需要重新创建

### 5. 配置应用

打开 `app/src/main/java/com/example/tradingplatform/data/auth/email/EmailService.kt`：

找到以下两行并替换：

```kotlin
// 第 18 行：替换为您的 SendGrid API Key
private const val API_KEY = "SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"  // 替换为步骤4中复制的 API Key

// 第 21 行：替换为您在 SendGrid 中验证的发件人邮箱
private const val FROM_EMAIL = "yourname@ucdconnect.ie"  // 替换为步骤3中验证的邮箱
```

**示例：**
```kotlin
private const val API_KEY = "SG.abc123def456ghi789jkl012mno345pqr678stu901vwx234"
private const val FROM_EMAIL = "xinghang.cao@ucdconnect.ie"
```

### 5. 安全建议

**重要**：不要将 API Key 提交到版本控制系统！

推荐做法：
1. 使用 `BuildConfig` 或环境变量
2. 将 API Key 添加到 `local.properties`（已加入 .gitignore）
3. 在构建时读取

### 6. 测试

1. 运行应用
2. 注册新用户
3. 检查邮箱（包括垃圾邮件文件夹）
4. 输入验证码完成验证

## 免费额度

SendGrid 免费版提供：
- 每天 100 封邮件
- 每月 40,000 封邮件（前 30 天）
- 之后每月 100 封邮件

对于开发测试，这个额度通常足够使用。

## 其他邮件服务选项

如果需要使用其他邮件服务，可以修改 `EmailService.kt`：

- **Mailgun**: https://www.mailgun.com/
- **AWS SES**: https://aws.amazon.com/ses/
- **Firebase Cloud Functions**: 使用 Node.js 发送邮件


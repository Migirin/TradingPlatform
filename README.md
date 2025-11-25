# TradingPlatform

二手交易平台 Android 应用

## 📱 项目简介

TradingPlatform 是一个功能完整的二手交易平台 Android 应用，支持商品浏览、发布、拍照识别、愿望清单管理、智能交换匹配等功能。

## ✨ 主要功能

- 🛍️ **商品管理**：浏览、发布、搜索商品
- 📸 **拍照识别**：使用 ML Kit 或百度 AI 识别商品并推荐相似商品
- ❤️ **愿望清单**：保存心仪商品，设置价格提醒
- 🔄 **智能匹配**：基于愿望清单的智能交换匹配系统
- 🏆 **成就系统**：交易成就与徽章系统
- 💬 **聊天功能**：买卖双方实时沟通
- 📖 **商品故事**：为商品添加背景故事

## 🛠️ 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **本地数据库**: Room Database
- **后端服务**: Supabase
- **相机**: CameraX
- **图像识别**: ML Kit / 百度 AI
- **网络请求**: Retrofit
- **异步处理**: Kotlin Coroutines & Flow

## 📋 开发环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17 或更高版本
- Gradle 8.0 或更高版本
- Android SDK API 33+
- 最低支持 Android 7.0 (API 24)

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/你的用户名/TradingPlatform.git
cd TradingPlatform
```

### 2. 配置 Supabase

1. 在 [Supabase](https://supabase.com) 创建项目
2. 复制项目 URL 和 API Key
3. 在 `local.properties` 文件中配置：

```properties
SUPABASE_URL=你的项目URL
SUPABASE_API_KEY=你的API密钥
```

### 3. 配置第三方 API（可选）

如果需要使用百度 AI 图像识别，在 `local.properties` 中添加：

```properties
BAIDU_API_KEY=你的百度API密钥
BAIDU_SECRET_KEY=你的百度Secret密钥
```

参考 `local.properties.example` 文件了解完整配置。

### 4. 运行项目

1. 使用 Android Studio 打开项目
2. 同步 Gradle
3. 连接 Android 设备或启动模拟器
4. 点击运行按钮

## 📁 项目结构

```
TradingPlatform/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/tradingplatform/
│   │   │   │   ├── data/          # 数据层
│   │   │   │   │   ├── items/     # 商品数据
│   │   │   │   │   ├── wishlist/  # 愿望清单
│   │   │   │   │   ├── vision/    # 图像识别
│   │   │   │   │   ├── supabase/  # Supabase API
│   │   │   │   │   └── local/     # Room 数据库
│   │   │   │   ├── ui/            # UI 层
│   │   │   │   │   ├── screens/   # 界面
│   │   │   │   │   ├── viewmodel/ # ViewModel
│   │   │   │   │   └── navigation/ # 导航
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/               # 资源文件
│   │   └── test/                  # 测试代码
│   └── build.gradle.kts
├── gradle/
└── build.gradle.kts
```

## 📚 文档

- [Supabase 设置指南](SUPABASE_SETUP.md)
- [第三方 API 使用指南](THIRD_PARTY_API_SETUP.md)
- [拍照识别功能说明](拍照识别商品功能说明.md)
- [愿望清单功能说明](愿望清单与降价提醒功能说明.md)
- [智能交换匹配系统说明](智能交换匹配系统说明.md)
- [GitHub 项目创建指南](GITHUB_SETUP.md)

## 🔧 数据库设置

### 创建 Supabase 表

项目包含以下 SQL 脚本：

- `SUPABASE_USERS_TABLE.sql` - 用户表
- `CREATE_WISHLIST_TABLE.sql` - 愿望清单表
- `ADD_CATEGORY_COLUMN.sql` - 添加类别列
- `ADD_STORY_COLUMN.sql` - 添加故事列

在 Supabase Dashboard 的 SQL Editor 中执行这些脚本。

## 🐛 问题排查

- [网络问题排查](NETWORK_TROUBLESHOOTING.md)
- [Supabase 401 错误修复](SUPABASE_401_FIX.md)
- [Supabase RLS 检查](SUPABASE_RLS_CHECK.md)
- [邮箱设置问题](EMAIL_TROUBLESHOOTING.md)

## 📝 许可证

[在此添加你的许可证]

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

[在此添加你的联系方式]

---

**注意**：本项目仅用于学习和教育目的。


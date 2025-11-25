# GitHub 项目创建指南

本指南将帮助你将 TradingPlatform 项目推送到 GitHub。

## 前置要求

1. **安装 Git**
   - 下载地址：https://git-scm.com/download/win
   - 安装后，在命令行中验证：`git --version`

2. **创建 GitHub 账号**
   - 访问：https://github.com
   - 注册并登录账号

## 步骤 1：在 GitHub 上创建新仓库

1. 登录 GitHub
2. 点击右上角的 **"+"** 按钮，选择 **"New repository"**
3. 填写仓库信息：
   - **Repository name**: `TradingPlatform`（或你喜欢的名称）
   - **Description**: `二手交易平台 Android 应用`
   - **Visibility**: 选择 **Public**（公开）或 **Private**（私有）
   - **不要**勾选 "Initialize this repository with a README"（我们已有项目）
4. 点击 **"Create repository"**

## 步骤 2：初始化本地 Git 仓库

在项目根目录（`D:\bjut1\Mobile Computing\TradingPlatform`）打开命令行或 PowerShell，执行以下命令：

```bash
# 1. 初始化 Git 仓库
git init

# 2. 添加所有文件到暂存区
git add .

# 3. 创建首次提交
git commit -m "Initial commit: TradingPlatform Android app"
```

## 步骤 3：连接到 GitHub 远程仓库

在 GitHub 仓库页面，复制仓库的 URL（例如：`https://github.com/你的用户名/TradingPlatform.git`），然后执行：

```bash
# 添加远程仓库（替换为你的实际 URL）
git remote add origin https://github.com/你的用户名/TradingPlatform.git

# 验证远程仓库
git remote -v
```

## 步骤 4：推送到 GitHub

```bash
# 推送代码到 GitHub（首次推送）
git branch -M main
git push -u origin main
```

**注意**：如果这是第一次使用 Git，可能需要配置用户信息：

```bash
git config --global user.name "你的名字"
git config --global user.email "你的邮箱@example.com"
```

## 步骤 5：身份验证

GitHub 已不再支持密码验证，你需要使用以下方式之一：

### 方式 1：使用 Personal Access Token（推荐）

1. 在 GitHub 上：
   - 点击右上角头像 → **Settings**
   - 左侧菜单 → **Developer settings**
   - **Personal access tokens** → **Tokens (classic)**
   - 点击 **"Generate new token (classic)"**
   - 设置名称和过期时间
   - 勾选 `repo` 权限
   - 点击 **"Generate token"**
   - **复制并保存 token**（只显示一次）

2. 推送时使用 token 作为密码：
   ```bash
   # 当提示输入密码时，使用 token 而不是 GitHub 密码
   git push -u origin main
   ```

### 方式 2：使用 GitHub Desktop（图形界面）

1. 下载：https://desktop.github.com/
2. 安装后登录 GitHub 账号
3. 使用图形界面添加仓库并推送

### 方式 3：使用 SSH 密钥（高级）

1. 生成 SSH 密钥：
   ```bash
   ssh-keygen -t ed25519 -C "你的邮箱@example.com"
   ```

2. 复制公钥内容（`~/.ssh/id_ed25519.pub`）

3. 在 GitHub 上：
   - Settings → **SSH and GPG keys**
   - 点击 **"New SSH key"**
   - 粘贴公钥并保存

4. 使用 SSH URL 添加远程仓库：
   ```bash
   git remote set-url origin git@github.com:你的用户名/TradingPlatform.git
   ```

## 后续操作

### 日常更新代码

```bash
# 查看更改状态
git status

# 添加更改的文件
git add .

# 提交更改
git commit -m "描述你的更改"

# 推送到 GitHub
git push
```

### 创建 README.md

建议在项目根目录创建 `README.md` 文件，描述项目：

```markdown
# TradingPlatform

二手交易平台 Android 应用

## 功能特性

- 商品浏览和发布
- 拍照识别商品
- 愿望清单管理
- 智能交换匹配
- 成就系统

## 技术栈

- Kotlin
- Jetpack Compose
- Room Database
- Supabase
- CameraX
- ML Kit / 百度 AI

## 开发环境

- Android Studio
- JDK 17+
- Gradle 8.0+

## 许可证

[你的许可证]
```

## 常见问题

### Q: 推送时提示 "Authentication failed"
**A**: 确保使用 Personal Access Token 而不是密码，或者配置 SSH 密钥。

### Q: 推送时提示 "remote: Permission denied"
**A**: 检查仓库 URL 是否正确，以及是否有推送权限。

### Q: 如何忽略某些文件？
**A**: 项目已有 `.gitignore` 文件，会自动忽略构建文件、本地配置等。如需添加，编辑 `.gitignore` 文件。

### Q: 如何回退到之前的版本？
**A**: 
```bash
# 查看提交历史
git log

# 回退到指定提交
git reset --hard <commit-hash>
```

## 有用的 Git 命令

```bash
# 查看提交历史
git log --oneline

# 查看文件更改
git diff

# 创建新分支
git checkout -b feature/新功能

# 切换分支
git checkout main

# 合并分支
git merge feature/新功能

# 查看远程仓库信息
git remote show origin
```

## 下一步

- 添加 `.github/workflows` 配置 CI/CD
- 创建 `CONTRIBUTING.md` 贡献指南
- 添加 `LICENSE` 许可证文件
- 设置 GitHub Pages（如果需要）

---

**提示**：如果遇到问题，可以查看 GitHub 官方文档：https://docs.github.com


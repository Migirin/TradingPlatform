# 上传项目到 GitHub 完整指南

## 当前状态

✅ Git 仓库已初始化  
✅ Git 用户信息已配置（用户名：Migirin，邮箱：2901626695@qq.com）  
✅ 项目文件已提交到本地仓库  
✅ 已创建 `.gitignore` 文件（自动忽略构建文件、本地配置等）  
⚠️ 需要创建或更新 GitHub 远程仓库

## 步骤 1：在 GitHub 上创建新仓库

1. 登录 GitHub：访问 https://github.com
2. 点击右上角的 **"+"** 按钮，选择 **"New repository"**
3. 填写仓库信息：
   - **Repository name**: `TradingPlatform`（或你喜欢的名称，例如：`TradingPlatform_Core`）
   - **Description**: `二手交易平台 Android 应用`
   - **Visibility**: 选择 **Public**（公开）或 **Private**（私有）
   - **不要**勾选 "Initialize this repository with a README"（我们已有项目）
   - **不要**勾选 "Add .gitignore"（我们已有）
   - **不要**勾选 "Choose a license"（可选，稍后添加）
4. 点击 **"Create repository"**

## 步骤 2：连接本地仓库到 GitHub

在项目目录打开 PowerShell，执行以下命令：

### 方法 A：如果创建了新仓库，更新远程 URL

```powershell
# 替换为你的实际仓库 URL
git remote set-url origin https://github.com/你的用户名/仓库名.git

# 验证远程仓库
git remote -v
```

### 方法 B：如果还没有配置远程仓库

```powershell
# 添加远程仓库（替换为你的实际 URL）
git remote add origin https://github.com/你的用户名/仓库名.git

# 验证远程仓库
git remote -v
```

**示例**：
```powershell
git remote set-url origin https://github.com/Migirin/TradingPlatform.git
```

## 步骤 3：推送到 GitHub

### 首次推送

```powershell
# 确保在 main 分支
git branch -M main

# 推送到 GitHub
git push -u origin main
```

### 身份验证

GitHub 已不再支持密码验证，你需要使用以下方式之一：

#### 方式 1：使用 Personal Access Token（推荐）

1. **创建 Token**：
   - 在 GitHub 上：点击右上角头像 → **Settings**
   - 左侧菜单 → **Developer settings**
   - **Personal access tokens** → **Tokens (classic)**
   - 点击 **"Generate new token (classic)"**
   - 设置名称（如：`TradingPlatform-Push`）和过期时间
   - 勾选 `repo` 权限（完整仓库访问权限）
   - 点击 **"Generate token"**
   - **复制并保存 token**（只显示一次！）

2. **推送时使用 token**：
   ```powershell
   git push -u origin main
   ```
   - 当提示输入用户名时：输入你的 GitHub 用户名
   - 当提示输入密码时：**粘贴刚才复制的 token**（不是 GitHub 密码）

#### 方式 2：使用 GitHub Desktop（图形界面，最简单）

1. 下载：https://desktop.github.com/
2. 安装后登录 GitHub 账号
3. 使用图形界面添加仓库并推送

#### 方式 3：使用 SSH 密钥（高级，推荐长期使用）

1. **生成 SSH 密钥**：
   ```powershell
   ssh-keygen -t ed25519 -C "2901626695@qq.com"
   ```
   - 按 Enter 使用默认路径
   - 可以设置密码或直接按 Enter（不设置密码）

2. **复制公钥内容**：
   ```powershell
   cat ~/.ssh/id_ed25519.pub
   ```
   或使用：
   ```powershell
   type $env:USERPROFILE\.ssh\id_ed25519.pub
   ```

3. **在 GitHub 上添加 SSH 密钥**：
   - Settings → **SSH and GPG keys**
   - 点击 **"New SSH key"**
   - Title: 输入一个名称（如：`我的电脑`）
   - Key: 粘贴刚才复制的公钥内容
   - 点击 **"Add SSH key"**

4. **使用 SSH URL 更新远程仓库**：
   ```powershell
   git remote set-url origin git@github.com:你的用户名/仓库名.git
   ```

5. **测试连接**：
   ```powershell
   ssh -T git@github.com
   ```

6. **推送**：
   ```powershell
   git push -u origin main
   ```

## 步骤 4：验证上传

推送成功后，访问你的 GitHub 仓库页面，应该能看到所有项目文件。

## 后续操作

### 日常更新代码

```powershell
# 查看更改状态
git status

# 添加更改的文件
git add .

# 提交更改
git commit -m "描述你的更改"

# 推送到 GitHub
git push
```

### 查看提交历史

```powershell
git log --oneline
```

### 查看远程仓库信息

```powershell
git remote show origin
```

## 常见问题

### Q: 推送时提示 "Authentication failed"
**A**: 
- 确保使用 Personal Access Token 而不是密码
- 或者配置 SSH 密钥
- 检查 token 是否过期

### Q: 推送时提示 "remote: Permission denied"
**A**: 
- 检查仓库 URL 是否正确
- 检查是否有推送权限
- 确认 token 有 `repo` 权限

### Q: 推送时提示 "Repository not found"
**A**: 
- 确认仓库名称和用户名正确
- 确认仓库已创建
- 检查是否有访问权限

### Q: 如何忽略某些文件？
**A**: 项目已有 `.gitignore` 文件，会自动忽略：
- 构建文件（`build/`、`*.apk` 等）
- 本地配置（`local.properties`）
- IDE 配置文件（`.idea/`、`.vscode/` 等）

如需添加更多忽略规则，编辑 `.gitignore` 文件。

## 当前项目信息

- **项目路径**: `D:\AndroidDev\TradingPlatform\TradingPlatform_Core_20251125_193851`
- **Git 用户**: Migirin (2901626695@qq.com)
- **当前分支**: main
- **已提交文件**: 120 个文件
- **远程仓库**: 需要更新或创建

## 下一步

1. ✅ 在 GitHub 上创建新仓库
2. ✅ 更新远程仓库 URL
3. ✅ 创建 Personal Access Token 或配置 SSH 密钥
4. ✅ 执行 `git push -u origin main` 推送代码

---

**提示**：如果遇到问题，可以查看项目中的其他指南文档：
- `GITHUB_SETUP.md` - GitHub 设置详细指南
- `GIT_CONFIG_GUIDE.md` - Git 配置指南
- `GIT_PUSH_REJECTED_FIX.md` - 推送问题排查



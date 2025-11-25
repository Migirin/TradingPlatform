# 修复 "src refspec main does not match any" 错误

## 问题原因

这个错误表示本地仓库还没有任何提交（commit），所以无法推送到 GitHub。

## 解决步骤

### 1. 检查 Git 仓库状态

```bash
git status
```

如果显示 "not a git repository"，需要先初始化：

```bash
git init
```

### 2. 添加所有文件到暂存区

```bash
git add .
```

### 3. 创建首次提交

```bash
git commit -m "Initial commit: TradingPlatform Android app"
```

### 4. 检查远程仓库 URL（重要！）

**注意**：你需要将 `https://github.com/你的用户名/TradingPlatform.git` 替换为你的实际 GitHub 仓库 URL。

查看当前远程仓库：
```bash
git remote -v
```

如果 URL 不正确，需要更新：
```bash
# 删除旧的远程仓库
git remote remove origin

# 添加正确的远程仓库（替换为你的实际 URL）
git remote add origin https://github.com/你的实际用户名/TradingPlatform.git
```

### 5. 重命名分支并推送

```bash
git branch -M main
git push -u origin main
```

## 完整操作流程

```bash
# 1. 初始化仓库（如果还没有）
git init

# 2. 添加所有文件
git add .

# 3. 创建首次提交
git commit -m "Initial commit: TradingPlatform Android app"

# 4. 检查/更新远程仓库 URL（替换为你的实际 URL）
git remote remove origin  # 如果之前添加过错误的 URL
git remote add origin https://github.com/你的实际用户名/TradingPlatform.git

# 5. 重命名分支
git branch -M main

# 6. 推送到 GitHub
git push -u origin main
```

## 如何获取正确的 GitHub 仓库 URL

1. 登录 GitHub
2. 进入你的仓库页面（或创建新仓库）
3. 点击绿色的 **"Code"** 按钮
4. 复制 HTTPS URL（格式：`https://github.com/用户名/仓库名.git`）

## 如果遇到身份验证问题

推送时可能会要求输入用户名和密码。GitHub 已不再支持密码，需要使用 **Personal Access Token**：

1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. 生成新 token，勾选 `repo` 权限
3. 推送时：
   - 用户名：你的 GitHub 用户名
   - 密码：使用刚才生成的 token（不是 GitHub 密码）

## 验证推送成功

推送成功后，访问你的 GitHub 仓库页面，应该能看到所有文件。


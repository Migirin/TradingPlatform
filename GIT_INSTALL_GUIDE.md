# Git 安装指南（Windows）

## 方法 1：使用 GitHub Desktop（最简单，推荐）

### 步骤：

1. **下载 GitHub Desktop**
   - 访问：https://desktop.github.com/
   - 点击 "Download for Windows"
   - 运行安装程序

2. **登录 GitHub 账号**
   - 打开 GitHub Desktop
   - 使用你的 GitHub 账号登录

3. **添加本地仓库**
   - 点击 **File** → **Add Local Repository**
   - 点击 **Choose...** 按钮
   - 选择项目目录：`D:\bjut1\Mobile Computing\TradingPlatform`
   - 点击 **Add repository**

4. **发布到 GitHub**
   - 点击 **Publish repository** 按钮（右上角）
   - 填写仓库名称（如：`TradingPlatform`）
   - 选择是否公开（Public/Private）
   - 点击 **Publish Repository**

完成！你的代码已经推送到 GitHub 了。

---

## 方法 2：安装 Git 命令行工具

### 步骤：

1. **下载 Git for Windows**
   - 访问：https://git-scm.com/download/win
   - 下载会自动开始，或点击 "Click here to download"

2. **安装 Git**
   - 运行下载的安装程序（如：`Git-2.xx.x-64-bit.exe`）
   - **重要**：在 "Select Components" 页面，确保勾选：
     - ✅ Git Bash Here
     - ✅ Git GUI Here
     - ✅ Associate .git* configuration files with the default text editor
     - ✅ Associate .sh files to be run with Bash
   
   - 在 "Choosing the default editor" 页面，选择你喜欢的编辑器（或使用默认的 Vim）
   
   - 在 "Adjusting your PATH environment" 页面，**选择**：
     - ✅ **"Git from the command line and also from 3rd-party software"**（重要！）
   
   - 其他选项使用默认设置即可
   - 点击 "Install" 开始安装

3. **验证安装**
   - 关闭当前的 PowerShell/命令行窗口
   - 重新打开 PowerShell 或命令提示符
   - 输入以下命令验证：
   ```bash
   git --version
   ```
   - 如果显示版本号（如：`git version 2.xx.x`），说明安装成功

4. **配置 Git（首次使用）**
   ```bash
   git config --global user.name "你的名字"
   git config --global user.email "你的邮箱@example.com"
   ```

5. **初始化并推送项目**
   在项目目录中执行：
   ```bash
   cd "D:\bjut1\Mobile Computing\TradingPlatform"
   git init
   git add .
   git commit -m "Initial commit: TradingPlatform Android app"
   git branch -M main
   git remote add origin https://github.com/你的用户名/TradingPlatform.git
   git push -u origin main
   ```

---

## 方法 3：使用 Android Studio 内置的 Git

如果你使用 Android Studio，它内置了 Git 支持：

1. **在 Android Studio 中**
   - 打开项目
   - 点击顶部菜单 **VCS** → **Enable Version Control Integration**
   - 选择 **Git**，点击 **OK**

2. **创建首次提交**
   - 点击顶部菜单 **VCS** → **Commit**
   - 选择所有文件
   - 输入提交信息："Initial commit"
   - 点击 **Commit**

3. **推送到 GitHub**
   - 点击顶部菜单 **VCS** → **Git** → **Push**
   - 如果还没有远程仓库，点击 **Define remote**
   - 输入 GitHub 仓库 URL：`https://github.com/你的用户名/TradingPlatform.git`
   - 点击 **Push**

---

## 推荐方案

**对于初学者**：推荐使用 **GitHub Desktop**（方法 1），因为它：
- ✅ 图形界面，操作简单
- ✅ 自动处理身份验证
- ✅ 可视化查看更改
- ✅ 无需记忆命令

**对于有经验的开发者**：使用 **Git 命令行**（方法 2），因为它：
- ✅ 更灵活
- ✅ 可以执行所有 Git 操作
- ✅ 适合自动化脚本

---

## 常见问题

### Q: 安装 Git 后仍然提示 "无法识别 git 命令"
**A**: 
1. 确保安装时选择了 "Add Git to PATH"
2. 关闭并重新打开 PowerShell/命令行
3. 如果还是不行，手动添加到 PATH：
   - 右键 "此电脑" → "属性" → "高级系统设置"
   - 点击 "环境变量"
   - 在 "系统变量" 中找到 "Path"，点击 "编辑"
   - 添加 Git 安装路径（通常是：`C:\Program Files\Git\cmd`）
   - 重启 PowerShell

### Q: 如何检查 Git 是否已安装？
**A**: 在 PowerShell 中输入：
```bash
git --version
```
如果显示版本号，说明已安装。

### Q: 安装 Git 时应该选择哪些选项？
**A**: 
- **PATH 环境变量**：选择 "Git from the command line and also from 3rd-party software"
- **默认编辑器**：选择你熟悉的编辑器（或使用默认）
- **行尾转换**：使用默认 "Checkout as-is, commit as-is"
- **其他选项**：使用默认即可

---

## 下一步

安装完成后，参考 `GITHUB_SETUP.md` 了解如何创建和推送 GitHub 项目。


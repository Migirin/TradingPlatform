# Git 用户配置指南

## 问题

Git 需要知道你是谁才能创建提交。需要配置用户名和邮箱。

## 解决方案

### 方法 1：全局配置（推荐，适用于所有项目）

```bash
git config --global user.name "你的名字"
git config --global user.email "你的邮箱@example.com"
```

**示例**：
```bash
git config --global user.name "张三"
git config --global user.email "zhangsan@example.com"
```

### 方法 2：仅当前项目配置（仅适用于这个项目）

```bash
git config user.name "你的名字"
git config user.email "你的邮箱@example.com"
```

## 配置说明

- **user.name**：可以是你的真实姓名、GitHub 用户名或任何你喜欢的名字
- **user.email**：
  - 如果使用 GitHub，建议使用你在 GitHub 注册的邮箱
  - 或者使用任何有效的邮箱地址
  - 这个邮箱会显示在提交记录中

## 验证配置

配置完成后，可以验证：

```bash
# 查看全局配置
git config --global --list

# 或查看特定配置
git config --global user.name
git config --global user.email
```

## 完整操作流程

```bash
# 1. 配置用户信息（替换为你的信息）
git config --global user.name "你的名字"
git config --global user.email "你的邮箱@example.com"

# 2. 验证配置
git config --global user.name
git config --global user.email

# 3. 创建提交
git commit -m "Initial commit: TradingPlatform Android app"

# 4. 推送到 GitHub
git push -u origin main
```

## 注意事项

1. **邮箱隐私**：如果你不想公开邮箱，GitHub 提供了隐私邮箱功能：
   - 格式：`用户名@users.noreply.github.com`
   - 在 GitHub → Settings → Emails 中可以找到

2. **修改配置**：如果以后想修改，重新运行配置命令即可

3. **查看配置**：使用 `git config --list` 查看所有配置


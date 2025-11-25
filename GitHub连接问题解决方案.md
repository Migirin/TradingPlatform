# GitHub 连接问题解决方案

## 错误信息
```
fatal: unable to access 'https://github.com/Migirin/TradingPlatform.git/': 
Recv failure: Connection was reset
```

## 问题原因

这个错误通常由以下原因引起：
1. **网络连接不稳定** - 连接在传输过程中被中断
2. **防火墙/代理限制** - 网络策略阻止了 HTTPS 连接
3. **SSL/TLS 证书问题** - 证书验证失败
4. **GitHub 访问受限** - 在某些地区访问 GitHub 可能不稳定
5. **Git 缓冲区设置过小** - 默认缓冲区可能不足以处理连接

## 解决方案

### 方案 1：增加 Git HTTP 缓冲区大小（推荐先试这个）

```powershell
# 增加 HTTP 缓冲区到 500MB
git config --global http.postBuffer 524288000

# 增加 HTTP 版本
git config --global http.version HTTP/1.1

# 增加超时时间
git config --global http.lowSpeedLimit 0
git config --global http.lowSpeedTime 999999
```

然后重试推送：
```powershell
git push -u origin main
```

### 方案 2：使用 SSH 代替 HTTPS（最稳定，强烈推荐）

SSH 连接通常比 HTTPS 更稳定，特别是在网络受限的环境中。

#### 步骤 1：检查是否已有 SSH 密钥

```powershell
# 检查是否存在 SSH 密钥
Test-Path $env:USERPROFILE\.ssh\id_ed25519
# 或
Test-Path $env:USERPROFILE\.ssh\id_rsa
```

#### 步骤 2：生成 SSH 密钥（如果没有）

```powershell
# 生成新的 SSH 密钥
ssh-keygen -t ed25519 -C "2901626695@qq.com"

# 按 Enter 使用默认路径
# 可以设置密码或直接按 Enter（不设置密码）
```

#### 步骤 3：复制公钥

```powershell
# 显示公钥内容
type $env:USERPROFILE\.ssh\id_ed25519.pub
```

#### 步骤 4：在 GitHub 上添加 SSH 密钥

1. 访问：https://github.com/settings/keys
2. 点击 **"New SSH key"**
3. **Title**: 输入名称（如：`我的电脑`）
4. **Key**: 粘贴刚才复制的公钥内容
5. 点击 **"Add SSH key"**

#### 步骤 5：测试 SSH 连接

```powershell
# 测试连接
ssh -T git@github.com

# 如果看到 "Hi Migirin! You've successfully authenticated..." 说明成功
```

#### 步骤 6：更改远程仓库 URL 为 SSH

```powershell
# 将 HTTPS URL 改为 SSH URL
git remote set-url origin git@github.com:Migirin/TradingPlatform.git

# 验证
git remote -v
```

#### 步骤 7：使用 SSH 推送

```powershell
git push -u origin main
```

### 方案 3：临时禁用 SSL 验证（仅用于测试，不推荐生产环境）

⚠️ **警告**：这会降低安全性，仅用于诊断问题。

```powershell
# 临时禁用 SSL 验证（仅当前仓库）
git config http.sslVerify false

# 尝试推送
git push -u origin main

# 推送成功后，重新启用 SSL 验证
git config http.sslVerify true
```

### 方案 4：配置代理（如果你使用代理）

如果你使用代理服务器，需要配置 Git：

```powershell
# HTTP 代理
git config --global http.proxy http://代理地址:端口

# HTTPS 代理
git config --global https.proxy https://代理地址:端口

# 如果需要认证
git config --global http.proxy http://用户名:密码@代理地址:端口
```

取消代理：
```powershell
git config --global --unset http.proxy
git config --global --unset https.proxy
```

### 方案 5：使用 GitHub 镜像（如果在中国大陆）

如果 GitHub 访问困难，可以使用镜像：

```powershell
# 使用 GitHub 镜像（示例，需要找到可用的镜像）
git remote set-url origin https://github.com.cnpmjs.org/Migirin/TradingPlatform.git
```

### 方案 6：分多次推送（如果文件太大）

如果项目文件很大，可以尝试：

```powershell
# 先推送少量文件测试
git push -u origin main --verbose

# 如果还是失败，可以尝试增加重试次数
git config --global http.postBuffer 524288000
git config --global http.maxRequestBuffer 100M
```

## 推荐操作顺序

1. **首先尝试方案 1**（增加缓冲区）- 最简单
2. **如果方案 1 失败，使用方案 2**（SSH）- 最稳定可靠
3. **如果 SSH 也失败，检查网络和代理设置**

## 快速诊断命令

```powershell
# 检查 Git 配置
git config --list | Select-String -Pattern "http|proxy|ssl"

# 测试 GitHub 连接
curl -I https://github.com

# 检查网络连接
ping github.com

# 查看远程仓库配置
git remote -v
```

## 常见问题

### Q: SSH 连接提示 "Permission denied"
**A**: 
- 检查 SSH 密钥是否正确添加到 GitHub
- 确认使用的是正确的 GitHub 用户名
- 尝试：`ssh -T git@github.com -v` 查看详细错误信息

### Q: 使用代理后还是连接失败
**A**: 
- 确认代理地址和端口正确
- 检查代理是否需要认证
- 尝试直接访问 GitHub 网站测试代理是否工作

### Q: 所有方法都失败了
**A**: 
- 检查防火墙设置
- 联系网络管理员
- 考虑使用 VPN 或更换网络环境
- 使用 GitHub Desktop 图形界面工具

## 当前建议

基于你的情况，我**强烈推荐使用方案 2（SSH）**，因为：
- SSH 连接更稳定
- 不需要每次输入密码/token
- 不受 HTTPS 连接重置影响
- 是 GitHub 推荐的方式

---

**下一步**：执行方案 1 或方案 2，然后重试推送。



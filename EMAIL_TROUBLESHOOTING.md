# 邮件发送问题排查指南

## 问题：一台电脑能收到邮件，另一台电脑收不到

### 可能的原因和解决方案

#### 1. SendGrid 免费账户限制

**SendGrid 免费版限制：**
- 每天最多 100 封邮件
- 每月 40,000 封（仅前 30 天）
- 之后每月 100 封

**检查方法：**
1. 登录 SendGrid Dashboard：https://app.sendgrid.com/
2. 查看 **Activity** → **Email Activity**
3. 检查是否有邮件发送失败的记录
4. 查看 **Settings** → **Account Details** → **Usage** 查看使用量

**解决方案：**
- 如果达到限制，需要等待第二天或升级账户
- 检查是否有其他应用也在使用同一个 API Key

#### 2. 网络连接问题

**检查方法：**
1. 在另一台电脑上测试网络连接
2. 检查是否能访问 SendGrid API：`https://api.sendgrid.com`
3. 检查防火墙是否阻止了网络请求

**解决方案：**
- 确保网络连接正常
- 检查防火墙设置
- 如果在公司/学校网络，可能需要配置代理

#### 3. SendGrid API Key 权限问题

**检查方法：**
1. 登录 SendGrid Dashboard
2. 进入 **Settings** → **API Keys**
3. 检查 API Key 的权限设置
4. 确认 API Key 有 **Mail Send** 权限

**解决方案：**
- 如果权限不足，重新创建 API Key 并选择 **Full Access** 或 **Restricted Access** → **Mail Send**

#### 4. 发件人邮箱验证问题

**检查方法：**
1. 登录 SendGrid Dashboard
2. 进入 **Settings** → **Sender Authentication**
3. 检查 **Single Sender Verification** 中的邮箱状态
4. 确认 `xinghang.cao@ucdconnect.ie` 已通过验证

**解决方案：**
- 如果邮箱未验证，需要重新验证
- 确保验证邮件没有被标记为垃圾邮件

#### 5. 邮件被标记为垃圾邮件

**检查方法：**
1. 检查收件人的垃圾邮件文件夹
2. 检查邮件是否被自动删除
3. 检查邮箱的过滤规则

**解决方案：**
- 提醒用户检查垃圾邮件文件夹
- 将发件人添加到联系人列表
- 在 SendGrid 中配置 SPF/DKIM（需要域名验证）

#### 6. 错误日志未显示

**检查方法：**
1. 在另一台电脑上运行应用
2. 查看 Logcat 中的 `EmailService` 日志
3. 查看是否有错误信息

**解决方案：**
- 查看 Logcat 中的详细错误信息
- 根据错误代码查找解决方案

### 诊断步骤

#### 步骤 1：检查 SendGrid Dashboard

1. 登录 SendGrid Dashboard
2. 查看 **Activity** → **Email Activity**
3. 检查邮件发送记录：
   - 是否有发送记录？
   - 发送状态是什么？（Delivered / Bounced / Blocked / Deferred）
   - 是否有错误信息？

#### 步骤 2：检查应用日志

在另一台电脑上：
1. 运行应用并尝试注册
2. 查看 Logcat，搜索 `EmailService`
3. 查看是否有错误日志：
   - `发送邮件失败: HTTP XXX`
   - `错误响应: ...`

#### 步骤 3：测试网络连接

在另一台电脑上测试：
```bash
# 测试 SendGrid API 是否可访问
curl -X GET "https://api.sendgrid.com/v3/user/profile" \
  -H "Authorization: Bearer YOUR_API_KEY"
```

#### 步骤 4：检查 SendGrid 账户状态

1. 登录 SendGrid Dashboard
2. 查看 **Settings** → **Account Details**
3. 检查账户状态：
   - 账户是否被暂停？
   - 是否有警告信息？
   - 使用量是否超限？

### 常见错误代码

- **401 Unauthorized**: API Key 无效或已过期
- **403 Forbidden**: API Key 权限不足或被限制
- **429 Too Many Requests**: 发送频率过高，达到限制
- **400 Bad Request**: 请求格式错误（可能是邮箱地址格式问题）

### 临时解决方案

如果问题暂时无法解决：

1. **使用开发者模式**：
   - 在注册界面点击"开发模式"按钮
   - 跳过邮箱验证，直接进入应用

2. **手动验证**：
   - 如果用户在其他设备注册过，可以在新设备上直接登录
   - 系统会自动从 Supabase 同步用户信息

3. **检查本地日志**：
   - 查看应用中的错误提示
   - 根据错误信息进行相应处理

### 联系支持

如果以上方法都无法解决问题：

1. **SendGrid 支持**：
   - 访问 SendGrid 支持页面
   - 提供详细的错误信息和日志

2. **检查代码**：
   - 确保所有电脑上的代码版本一致
   - 确保 API Key 配置正确








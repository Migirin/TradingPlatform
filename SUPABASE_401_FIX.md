# Supabase 401 错误修复指南

## 问题
API key 正确，但仍然出现 `HTTP 401 - Invalid API key` 错误。

## 可能的原因和解决方案

### 1. 检查 Legacy API Keys 是否被禁用

1. **在 Supabase Dashboard 中：**
   - 进入 Settings → API Keys
   - 点击 "Legacy API Keys" 标签页
   - 查看页面底部是否有 "Disable JWT-based API keys" 按钮
   - 如果显示为已禁用，请**启用它**

2. **如果 Legacy API Keys 被禁用：**
   - 点击 "Enable JWT-based API keys" 或类似按钮
   - 等待几秒钟让更改生效
   - 重新测试应用

### 2. 验证 API Key 格式

确认你的 API key：
- 以 `eyJ` 开头（JWT 格式）
- 长度约 200+ 个字符
- 没有多余的空格或换行符

当前使用的 key：
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJzeGx6ZWZ6cWZicGN3dXhjb2VrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI0OTU2MjUsImV4cCI6MjA3ODA3MTYyNX0.M3M6LsKbkt2b1Q6xZjDRKlTN1gE3q5LzyD8nbC6GPlY
```

### 3. 检查项目状态

1. **确认项目是否正常运行：**
   - 在 Supabase Dashboard 中，查看项目状态
   - 确认项目没有暂停或出现问题

2. **检查 Project URL：**
   - 当前配置：`https://bsxlzefzqfbpcwuxcoek.supabase.co`
   - 在 Settings → API 页面，确认 Project URL 是否匹配

### 4. 测试 API Key 是否有效

可以使用以下方法测试：

1. **使用 curl 命令测试（在命令行中）：**
```bash
curl -X GET "https://bsxlzefzqfbpcwuxcoek.supabase.co/rest/v1/items?select=*" \
  -H "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJzeGx6ZWZ6cWZicGN3dXhjb2VrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI0OTU2MjUsImV4cCI6MjA3ODA3MTYyNX0.M3M6LsKbkt2b1Q6xZjDRKlTN1gE3q5LzyD8nbC6GPlY" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJzeGx6ZWZ6cWZicGN3dXhjb2VrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI0OTU2MjUsImV4cCI6MjA3ODA3MTYyNX0.M3M6LsKbkt2b1Q6xZjDRKlTN1gE3q5LzyD8nbC6GPlY"
```

如果返回 401，说明 API key 确实无效。
如果返回 200 或空数组，说明 API key 有效。

### 5. 联系 Supabase 支持

如果以上方法都不行：
1. 在 Supabase Dashboard 中点击 "Contact support"
2. 说明你遇到了 401 错误，即使 API key 看起来正确
3. 提供项目 ID：`bsxlzefzqfbpcwuxcoek`

## 临时解决方案

如果 Legacy API Keys 确实无法使用，可以考虑：
1. 使用新的 API Keys 系统（需要修改代码）
2. 或者暂时只使用本地数据库（不同步到 Supabase）

## 下一步

请先检查 Legacy API Keys 是否被禁用，这是最常见的原因。






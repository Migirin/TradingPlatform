# Supabase API Key 修复指南

## 问题
错误信息：`HTTP 401 - Invalid API key`

这说明当前的 API key 无效或已过期。

## 解决步骤

### 1. 获取正确的 API Key

1. **打开 Supabase Dashboard**
   - 访问 https://supabase.com/dashboard
   - 登录你的账号

2. **进入项目设置**
   - 选择你的项目（项目ID: `bsxlzefzqfbpcwuxcoek`）
   - 点击左侧菜单的 **Settings**（齿轮图标）
   - 点击 **API**

3. **复制 anon public key**
   - 在 **Project API keys** 部分
   - 找到 **anon public** key（不是 service_role key！）
   - 点击右侧的 **眼睛图标** 显示完整 key
   - 点击 **复制按钮** 复制完整的 key

### 2. 更新代码中的 API Key

1. **打开文件**
   - 打开 `app/src/main/java/com/example/tradingplatform/data/supabase/SupabaseConfig.kt`

2. **替换 ANON_KEY**
   - 找到第 23 行的 `const val ANON_KEY = "..."`
   - 将引号中的内容替换为你刚复制的 **anon public** key
   - 确保：
     - 没有多余的空格
     - 没有换行符
     - 完整复制了整个 key（通常很长，以 `eyJ` 开头）

3. **保存文件**
   - 按 `Ctrl+S` 保存

### 3. 验证 Project URL

确认 `PROJECT_URL` 是否正确：
- 当前值：`https://bsxlzefzqfbpcwuxcoek.supabase.co`
- 在 Supabase Dashboard 的 Settings → API 页面，确认 **Project URL** 是否匹配

### 4. 重新编译和测试

1. **重新编译**
   - 在 Android Studio 中点击 "Build" → "Rebuild Project"

2. **运行应用**
   - 点击 "Run" 或按 `Shift+F10`

3. **测试发布商品**
   - 发布一个新商品
   - 查看 Logcat，应该看到 `商品已同步到 Supabase` 的成功日志

4. **验证数据**
   - 在 Supabase Dashboard 的 Table Editor 中
   - 查看 `items` 表，应该能看到新发布的商品

## 常见问题

### Q: 我复制了 service_role key，可以吗？
**A: 不可以！** 必须使用 **anon public** key。service_role key 有更高的权限，不应该在客户端使用。

### Q: API key 很长，我担心复制不完整
**A: 使用复制按钮**，不要手动选择文本。确保复制的是完整的 key（通常 200+ 个字符）。

### Q: 更新后还是 401 错误
**A: 检查以下几点：**
1. 确认使用的是 **anon public** key，不是 service_role
2. 确认 key 没有多余的空格或换行
3. 确认 Project URL 正确
4. 重新编译项目（不要只是运行，要重新编译）

### Q: 如何确认 API key 是否正确？
**A: 在 Logcat 中查看：**
- 搜索 `SupabaseClient`
- 查看 `API Key (前10字符): eyJhbGciOi...`
- 这个前 10 个字符应该和你在 Supabase Dashboard 中看到的 anon public key 的前 10 个字符一致






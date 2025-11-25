# Supabase 配置指南

## 1. 创建 Supabase 项目

1. 访问 https://supabase.com
2. 注册/登录账号
3. 点击 "New Project" 创建新项目
4. 填写项目信息：
   - **Name**: 项目名称（例如：TradingPlatform）
   - **Database Password**: 设置数据库密码（请记住）
   - **Region**: 选择离你最近的区域
5. 等待项目创建完成（约 2 分钟）

## 2. 获取 API 密钥

1. 在项目 Dashboard 中，点击左侧菜单的 **Settings** (齿轮图标)
2. 点击 **API**
3. 找到以下信息：
   - **Project URL**: 例如 `https://xxxxx.supabase.co`
   - **anon public key**: 这是公开的 API Key（用于客户端）

## 3. 配置应用

打开 `app/src/main/java/com/example/tradingplatform/data/supabase/SupabaseConfig.kt`，替换以下值：

```kotlin
const val PROJECT_URL = "https://your-project.supabase.co"  // 替换为你的 Project URL
const val ANON_KEY = "your-anon-key-here"  // 替换为你的 anon public key
```

## 4. 创建数据库表

在 Supabase Dashboard 中：

1. 点击左侧菜单的 **SQL Editor**
2. 执行以下 SQL 创建 `items` 表：

```sql
-- 创建 items 表
CREATE TABLE items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    description TEXT,
    image_url TEXT,
    phone_number TEXT NOT NULL,
    owner_uid TEXT NOT NULL,
    owner_email TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 创建索引以提高查询性能
CREATE INDEX idx_items_created_at ON items(created_at DESC);
CREATE INDEX idx_items_owner_uid ON items(owner_uid);

-- 启用 Row Level Security (RLS)
ALTER TABLE items ENABLE ROW LEVEL SECURITY;

-- 创建策略：允许所有人读取
CREATE POLICY "Allow public read access" ON items
    FOR SELECT USING (true);

-- 创建策略：允许所有人插入
CREATE POLICY "Allow public insert" ON items
    FOR INSERT WITH CHECK (true);

-- 创建策略：允许所有人更新（仅开发者模式）
CREATE POLICY "Allow public update" ON items
    FOR UPDATE USING (true);

-- 创建策略：允许所有人删除（仅开发者模式）
CREATE POLICY "Allow public delete" ON items
    FOR DELETE USING (true);
```

## 5. 创建 Storage Bucket（用于图片存储）

1. 点击左侧菜单的 **Storage**
2. 点击 **New bucket**
3. 填写信息：
   - **Name**: `item_images`
   - **Public bucket**: ✅ 勾选（允许公开访问）
4. 点击 **Create bucket**

### 设置 Storage 策略

在 Storage 的 **Policies** 标签页，为 `item_images` bucket 创建策略：

```sql
-- 允许所有人读取
CREATE POLICY "Allow public read access" ON storage.objects
    FOR SELECT USING (bucket_id = 'item_images');

-- 允许所有人上传
CREATE POLICY "Allow public upload" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'item_images');

-- 允许所有人删除
CREATE POLICY "Allow public delete" ON storage.objects
    FOR DELETE USING (bucket_id = 'item_images');
```

## 6. 测试连接

配置完成后，运行应用并尝试发布商品。如果配置正确，商品应该会同步到 Supabase。

## 注意事项

- **安全性**: 当前配置允许所有人读写数据，适合开发阶段。生产环境应该使用更严格的 RLS 策略。
- **API Key**: `anon key` 是公开的，但 Supabase 的 RLS 策略会保护数据安全。
- **网络**: 如果遇到网络问题，可能需要配置代理或使用 VPN。

## 故障排除

### 问题：无法连接到 Supabase
- 检查 Project URL 和 API Key 是否正确
- 检查网络连接
- 查看 Logcat 中的错误信息

### 问题：图片上传失败
- 确认 Storage bucket 已创建
- 确认 Storage 策略已设置
- 检查图片大小（建议小于 5MB）

### 问题：数据未同步
- 检查数据库表是否已创建
- 检查 RLS 策略是否正确
- 查看 Supabase Dashboard 的 Logs 查看错误信息






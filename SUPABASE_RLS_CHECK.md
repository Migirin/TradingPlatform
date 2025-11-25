# Supabase RLS 策略检查

## 检查步骤

1. **在 Supabase Dashboard 中：**
   - 点击左侧菜单的 **Table Editor**
   - 选择 `items` 表
   - 点击右上角的 **RLS policies** (应该显示 "4 policies")

2. **如果没有 4 个策略，请在 SQL Editor 中执行以下 SQL：**

```sql
-- 删除旧策略（如果存在）
DROP POLICY IF EXISTS "Allow public read access" ON items;
DROP POLICY IF EXISTS "Allow public insert" ON items;
DROP POLICY IF EXISTS "Allow public update" ON items;
DROP POLICY IF EXISTS "Allow public delete" ON items;

-- 创建策略：允许所有人读取
CREATE POLICY "Allow public read access" ON items
    FOR SELECT USING (true);

-- 创建策略：允许所有人插入
CREATE POLICY "Allow public insert" ON items
    FOR INSERT WITH CHECK (true);

-- 创建策略：允许所有人更新
CREATE POLICY "Allow public update" ON items
    FOR UPDATE USING (true);

-- 创建策略：允许所有人删除
CREATE POLICY "Allow public delete" ON items
    FOR DELETE USING (true);
```

3. **检查表结构是否正确：**

在 SQL Editor 中执行：

```sql
-- 查看表结构
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'items'
ORDER BY ordinal_position;
```

应该看到以下字段：
- `id` (uuid)
- `title` (text)
- `price` (double precision)
- `description` (text)
- `image_url` (text)
- `phone_number` (text)
- `owner_uid` (text)
- `owner_email` (text)
- `created_at` (timestamp with time zone)
- `updated_at` (timestamp with time zone)

4. **如果表结构不完整，执行以下 SQL 修复：**

```sql
-- 添加缺失的字段（如果不存在）
ALTER TABLE items 
    ADD COLUMN IF NOT EXISTS phone_number TEXT NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS owner_uid TEXT NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS owner_email TEXT NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();
```






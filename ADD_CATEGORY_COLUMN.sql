-- 为 items 表添加 category 列（商品类别）
-- 在 Supabase Dashboard 的 SQL Editor 中执行此脚本

-- 添加 category 列（允许为空，类型为 TEXT）
ALTER TABLE items 
ADD COLUMN IF NOT EXISTS category TEXT;

-- 添加注释说明
COMMENT ON COLUMN items.category IS '商品类别，例如：电子产品、服装配饰、图书文具等';

-- 注意：如果表中已有数据，新添加的列默认值为 NULL
-- 如果需要为现有数据设置默认值，可以执行：
-- UPDATE items SET category = '' WHERE category IS NULL;





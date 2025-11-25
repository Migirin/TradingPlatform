-- 为 items 表添加 story 列（商品故事/背景）
-- 在 Supabase Dashboard 的 SQL Editor 中执行此脚本

-- 添加 story 列（允许为空，类型为 TEXT）
ALTER TABLE items 
ADD COLUMN IF NOT EXISTS story TEXT;

-- 添加注释说明
COMMENT ON COLUMN items.story IS '商品故事/背景，卖家可以分享商品背后的故事';

-- 注意：如果表中已有数据，新添加的列默认值为 NULL
-- 如果需要为现有数据设置默认值，可以执行：
-- UPDATE items SET story = '' WHERE story IS NULL;




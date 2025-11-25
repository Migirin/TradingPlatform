-- 创建愿望清单表
CREATE TABLE IF NOT EXISTS wishlist (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id TEXT NOT NULL,
  user_email TEXT NOT NULL,
  title TEXT NOT NULL,
  category TEXT,
  min_price DOUBLE PRECISION DEFAULT 0,
  max_price DOUBLE PRECISION DEFAULT 0,
  target_price DOUBLE PRECISION DEFAULT 0,
  item_id TEXT,
  enable_price_alert BOOLEAN DEFAULT false,
  description TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_wishlist_user_id ON wishlist(user_id);
CREATE INDEX IF NOT EXISTS idx_wishlist_item_id ON wishlist(item_id);
CREATE INDEX IF NOT EXISTS idx_wishlist_category ON wishlist(category);

-- 启用 Row Level Security (RLS)
ALTER TABLE wishlist ENABLE ROW LEVEL SECURITY;

-- 创建策略：允许所有人读取（用于匹配和查看）
CREATE POLICY "Allow public read access" ON wishlist
    FOR SELECT USING (true);

-- 创建策略：允许所有人插入（添加愿望清单）
CREATE POLICY "Allow public insert" ON wishlist
    FOR INSERT WITH CHECK (true);

-- 创建策略：允许所有人更新（更新自己的愿望清单）
CREATE POLICY "Allow public update" ON wishlist
    FOR UPDATE USING (true);

-- 创建策略：允许所有人删除（删除自己的愿望清单）
CREATE POLICY "Allow public delete" ON wishlist
    FOR DELETE USING (true);


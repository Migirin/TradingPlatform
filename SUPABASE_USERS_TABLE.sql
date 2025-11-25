-- 创建 users 表（Supabase）
-- 在 Supabase Dashboard 的 SQL Editor 中执行此脚本

-- 创建 users 表
CREATE TABLE IF NOT EXISTS users (
    email TEXT PRIMARY KEY,
    uid TEXT NOT NULL UNIQUE,
    display_name TEXT,
    email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_users_uid ON users(uid);
CREATE INDEX IF NOT EXISTS idx_users_email_verified ON users(email_verified);

-- 启用 Row Level Security (RLS)
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- 创建策略：允许所有人读取（公开信息）
CREATE POLICY "Allow public read access" ON users
    FOR SELECT USING (true);

-- 创建策略：允许所有人插入（注册）
CREATE POLICY "Allow public insert" ON users
    FOR INSERT WITH CHECK (true);

-- 创建策略：允许所有人更新（更新自己的信息）
CREATE POLICY "Allow public update" ON users
    FOR UPDATE USING (true);

-- 创建策略：不允许删除（保护用户数据）
-- 如果需要删除功能，可以添加：
-- CREATE POLICY "Allow public delete" ON users
--     FOR DELETE USING (true);




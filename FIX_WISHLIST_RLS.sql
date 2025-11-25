-- 修复愿望清单表的 RLS 策略
-- 如果已经创建了错误的策略，先删除它们

-- 删除所有可能存在的策略（包括同名的）
DROP POLICY IF EXISTS "Users can view their own wishlist" ON wishlist;
DROP POLICY IF EXISTS "Users can insert their own wishlist" ON wishlist;
DROP POLICY IF EXISTS "Users can update their own wishlist" ON wishlist;
DROP POLICY IF EXISTS "Users can delete their own wishlist" ON wishlist;
DROP POLICY IF EXISTS "Users can view all wishlists for matching" ON wishlist;
DROP POLICY IF EXISTS "Allow public read access" ON wishlist;
DROP POLICY IF EXISTS "Allow public insert" ON wishlist;
DROP POLICY IF EXISTS "Allow public update" ON wishlist;
DROP POLICY IF EXISTS "Allow public delete" ON wishlist;

-- 创建新的公开访问策略
CREATE POLICY "Allow public read access" ON wishlist
    FOR SELECT USING (true);

CREATE POLICY "Allow public insert" ON wishlist
    FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow public update" ON wishlist
    FOR UPDATE USING (true);

CREATE POLICY "Allow public delete" ON wishlist
    FOR DELETE USING (true);


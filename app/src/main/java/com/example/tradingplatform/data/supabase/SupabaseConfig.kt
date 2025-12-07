package com.example.tradingplatform.data.supabase

/**
 * Supabase 配置
 * 
 * 配置步骤：
 * 1. 访问 https://supabase.com 注册账号
 * 2. 创建新项目
 * 3. 在项目设置中找到：
 *    - Project URL (例如: https://xxxxx.supabase.co)
 *    - anon/public key (API Key)
 * 4. 将下面的值替换为你的项目信息
 */
object SupabaseConfig {
    // TODO: 替换为你的 Supabase Project URL
    // 格式：https://{项目ID}.supabase.co
    // 从地址栏可以看到项目ID，例如：https://supabase.com/dashboard/project/bsxlzefzqfbpcwuxcoek
    // 那么 Project URL 就是：https://bsxlzefzqfbpcwuxcoek.supabase.co
    const val PROJECT_URL = "https://bsxlzefzqfbpcwuxcoek.supabase.co"
    
    // TODO: 替换为你的 Supabase anon/public key
    // 在 Settings -> API Keys 页面，复制 "anon public" key
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJzeGx6ZWZ6cWZicGN3dXhjb2VrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI0OTU2MjUsImV4cCI6MjA3ODA3MTYyNX0.M3M6LsKbkt2b1Q6xZjDRKlTN1gE3q5LzyD8nbC6GPlY"
    
    // 数据库表名
    const val TABLE_ITEMS = "items"
    const val TABLE_USERS = "users"
    const val TABLE_CHAT_MESSAGES = "chat_messages"
    
    // Storage bucket 名称
    const val STORAGE_BUCKET_IMAGES = "item_images"
}


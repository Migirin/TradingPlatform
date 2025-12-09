package com.example.tradingplatform.data.supabase

/**
 * Supabase 配置 / Supabase Configuration
 * 
 * 配置步骤 / Configuration Steps:
 * 1. 访问 https://supabase.com 注册账号 / Visit https://supabase.com to register an account
 * 2. 创建新项目 / Create a new project
 * 3. 在项目设置中找到：/ Find in project settings:
 *    - Project URL (例如: https://xxxxx.supabase.co) / Project URL (e.g.: https://xxxxx.supabase.co)
 *    - anon/public key (API Key) / anon/public key (API Key)
 * 4. 将下面的值替换为你的项目信息 / Replace the values below with your project information
 */
object SupabaseConfig {
    // TODO: 替换为你的 Supabase Project URL / Replace with your Supabase Project URL
    // 格式：https://{项目ID}.supabase.co / Format: https://{projectId}.supabase.co
    // 从地址栏可以看到项目ID，例如：https://supabase.com/dashboard/project/bsxlzefzqfbpcwuxcoek / You can see the project ID in the address bar, e.g.: https://supabase.com/dashboard/project/bsxlzefzqfbpcwuxcoek
    // 那么 Project URL 就是：https://bsxlzefzqfbpcwuxcoek.supabase.co / Then the Project URL is: https://bsxlzefzqfbpcwuxcoek.supabase.co
    const val PROJECT_URL = "https://bsxlzefzqfbpcwuxcoek.supabase.co"
    
    // TODO: 替换为你的 Supabase anon/public key / Replace with your Supabase anon/public key
    // 在 Settings -> API Keys 页面，复制 "anon public" key / In Settings -> API Keys page, copy the "anon public" key
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJzeGx6ZWZ6cWZicGN3dXhjb2VrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI0OTU2MjUsImV4cCI6MjA3ODA3MTYyNX0.M3M6LsKbkt2b1Q6xZjDRKlTN1gE3q5LzyD8nbC6GPlY"
    
    // 数据库表名 / Database table names
    const val TABLE_ITEMS = "items"
    const val TABLE_USERS = "users"
    const val TABLE_CHAT_MESSAGES = "chat_messages"
    
    // Storage bucket 名称 / Storage bucket name
    const val STORAGE_BUCKET_IMAGES = "item_images"
}


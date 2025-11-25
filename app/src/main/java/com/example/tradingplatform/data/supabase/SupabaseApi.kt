package com.example.tradingplatform.data.supabase

import com.example.tradingplatform.data.items.Item
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Path

/**
 * Supabase REST API 接口
 * 使用 Retrofit 调用 Supabase PostgREST API
 */
interface SupabaseApi {
    
    /**
     * 获取所有商品
     */
    @GET("items?select=*&order=created_at.desc")
    suspend fun getItems(
        @Query("limit") limit: Int = 50
    ): Response<List<SupabaseItem>>
    
    /**
     * 根据ID获取商品
     * Supabase PostgREST 使用 id=eq.{value} 格式
     */
    @GET("items")
    suspend fun getItemById(
        @Query("id") idFilter: String, // 格式：eq.{实际ID值}，例如：eq.123e4567-e89b-12d3-a456-426614174000
        @Query("select") select: String = "*"
    ): Response<List<SupabaseItem>>
    
    /**
     * 创建商品
     * 注意：请求头由 SupabaseClient 的拦截器统一添加
     */
    @POST("items")
    suspend fun createItem(
        @Body item: CreateItemRequest
    ): Response<List<SupabaseItem>>
    
    /**
     * 更新商品
     */
    @PATCH("items")
    suspend fun updateItem(
        @Query("id") idFilter: String, // 格式：eq.{id}
        @Body item: UpdateItemRequest
    ): Response<List<SupabaseItem>>
    
    /**
     * 删除商品
     */
    @HTTP(method = "DELETE", path = "items", hasBody = false)
    suspend fun deleteItem(
        @Query("id") idFilter: String // 格式：eq.{id}
    ): Response<Unit>
    
    /**
     * 创建用户
     */
    @POST("users")
    suspend fun createUser(
        @Body user: CreateUserRequest
    ): Response<List<SupabaseUser>>
    
    /**
     * 根据邮箱获取用户
     */
    @GET("users")
    suspend fun getUserByEmail(
        @Query("email") emailFilter: String, // 格式：eq.{email}
        @Query("select") select: String = "*"
    ): Response<List<SupabaseUser>>
    
    /**
     * 更新用户
     */
    @PATCH("users")
    suspend fun updateUser(
        @Query("email") emailFilter: String, // 格式：eq.{email}
        @Body user: UpdateUserRequest
    ): Response<List<SupabaseUser>>
    
    // ========== 愿望清单 API ==========
    
    /**
     * 获取所有愿望清单（用于匹配）
     */
    @GET("wishlist?select=*&order=created_at.desc")
    suspend fun getAllWishlistItems(
        @Query("limit") limit: Int = 100
    ): Response<List<SupabaseWishlistItem>>
    
    /**
     * 根据用户ID获取愿望清单
     */
    @GET("wishlist")
    suspend fun getWishlistByUser(
        @Query("user_id") userIdFilter: String, // 格式：eq.{userId}
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<SupabaseWishlistItem>>
    
    /**
     * 根据ID获取愿望清单项
     */
    @GET("wishlist")
    suspend fun getWishlistItemById(
        @Query("id") idFilter: String, // 格式：eq.{id}
        @Query("select") select: String = "*"
    ): Response<List<SupabaseWishlistItem>>
    
    /**
     * 创建愿望清单项
     */
    @POST("wishlist")
    suspend fun createWishlistItem(
        @Body item: CreateWishlistItemRequest
    ): Response<List<SupabaseWishlistItem>>
    
    /**
     * 更新愿望清单项
     */
    @PATCH("wishlist")
    suspend fun updateWishlistItem(
        @Query("id") idFilter: String, // 格式：eq.{id}
        @Body item: UpdateWishlistItemRequest
    ): Response<List<SupabaseWishlistItem>>
    
    /**
     * 删除愿望清单项
     */
    @HTTP(method = "DELETE", path = "wishlist", hasBody = false)
    suspend fun deleteWishlistItem(
        @Query("id") idFilter: String // 格式：eq.{id}
    ): Response<Unit>
}

/**
 * 创建商品的请求数据类
 */
data class CreateItemRequest(
    @SerializedName("id")
    val id: String? = null, // 可选，如果为空则由数据库生成
    @SerializedName("title")
    val title: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("category")
    val category: String? = null, // 商品类别
    @SerializedName("story")
    val story: String? = null, // 商品故事/背景
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("owner_uid")
    val ownerUid: String,
    @SerializedName("owner_email")
    val ownerEmail: String
)

/**
 * 更新商品的请求数据类
 */
data class UpdateItemRequest(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("price")
    val price: Double? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("story")
    val story: String? = null, // 商品故事/背景
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("phone_number")
    val phoneNumber: String? = null
)

/**
 * 创建用户的请求数据类
 */
data class CreateUserRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("uid")
    val uid: String,
    @SerializedName("display_name")
    val displayName: String? = null,
    @SerializedName("email_verified")
    val emailVerified: Boolean = false
)

/**
 * 更新用户的请求数据类
 */
data class UpdateUserRequest(
    @SerializedName("email_verified")
    val emailVerified: Boolean? = null,
    @SerializedName("display_name")
    val displayName: String? = null
)

/**
 * Supabase 商品数据类（对应数据库表结构）
 */
data class SupabaseItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("description")
    val description: String? = null, // 允许为 null
    @SerializedName("category")
    val category: String? = null, // 商品类别
    @SerializedName("story")
    val story: String? = null, // 商品故事/背景
    @SerializedName("image_url")
    val imageUrl: String? = null, // 允许为 null
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("owner_uid")
    val ownerUid: String,
    @SerializedName("owner_email")
    val ownerEmail: String,
    @SerializedName("created_at")
    val createdAt: String, // ISO 8601 格式
    @SerializedName("updated_at")
    val updatedAt: String
) {
    fun toItem(): Item {
        // Supabase 返回的时间格式可能是 ISO 8601 或 PostgreSQL timestamp
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        val dateFormatWithMs = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US)
        val dateFormatWithZ = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        
        fun parseDate(dateStr: String): java.util.Date {
            return try {
                dateFormatWithZ.parse(dateStr) ?: dateFormatWithMs.parse(dateStr) ?: dateFormat.parse(dateStr) ?: java.util.Date()
            } catch (e: Exception) {
                java.util.Date()
            }
        }
        
        return Item(
            id = id,
            title = title,
            price = price,
            description = description ?: "",
            category = category ?: "",
            story = story ?: "",
            imageUrl = imageUrl ?: "",
            phoneNumber = phoneNumber,
            ownerUid = ownerUid,
            ownerEmail = ownerEmail,
            createdAt = parseDate(createdAt),
            updatedAt = parseDate(updatedAt)
        )
    }
    
    companion object {
        fun fromItem(item: Item): CreateItemRequest {
            // 注意：created_at 和 updated_at 由数据库自动生成，不需要发送
            return CreateItemRequest(
                id = if (item.id.isNotEmpty()) item.id else null,
                title = item.title,
                price = item.price,
                description = item.description.ifEmpty { null },
                category = item.category.ifEmpty { null },
                story = item.story.ifEmpty { null },
                imageUrl = item.imageUrl.ifEmpty { null },
                phoneNumber = item.phoneNumber,
                ownerUid = item.ownerUid,
                ownerEmail = item.ownerEmail
            )
        }
    }
}

/**
 * Supabase 用户数据类（对应数据库表结构）
 */
data class SupabaseUser(
    @SerializedName("email")
    val email: String,
    @SerializedName("uid")
    val uid: String,
    @SerializedName("display_name")
    val displayName: String? = null,
    @SerializedName("email_verified")
    val emailVerified: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String, // ISO 8601 格式
    @SerializedName("updated_at")
    val updatedAt: String
) {
    companion object {
        fun fromLocalUser(user: com.example.tradingplatform.data.local.User): CreateUserRequest {
            return CreateUserRequest(
                email = user.email,
                uid = user.uid,
                displayName = user.displayName.ifEmpty { null },
                emailVerified = user.emailVerified
            )
        }
    }
}

/**
 * 创建愿望清单项的请求数据类
 */
data class CreateWishlistItemRequest(
    @SerializedName("id")
    val id: String? = null, // 可选，如果为空则由数据库生成
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_email")
    val userEmail: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("min_price")
    val minPrice: Double = 0.0,
    @SerializedName("max_price")
    val maxPrice: Double = 0.0,
    @SerializedName("target_price")
    val targetPrice: Double = 0.0,
    @SerializedName("item_id")
    val itemId: String? = null,
    @SerializedName("enable_price_alert")
    val enablePriceAlert: Boolean = false,
    @SerializedName("description")
    val description: String? = null
)

/**
 * 更新愿望清单项的请求数据类
 */
data class UpdateWishlistItemRequest(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("min_price")
    val minPrice: Double? = null,
    @SerializedName("max_price")
    val maxPrice: Double? = null,
    @SerializedName("target_price")
    val targetPrice: Double? = null,
    @SerializedName("item_id")
    val itemId: String? = null,
    @SerializedName("enable_price_alert")
    val enablePriceAlert: Boolean? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null // ISO 8601 格式
)

/**
 * Supabase 愿望清单数据类（对应数据库表结构）
 */
data class SupabaseWishlistItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_email")
    val userEmail: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("min_price")
    val minPrice: Double = 0.0,
    @SerializedName("max_price")
    val maxPrice: Double = 0.0,
    @SerializedName("target_price")
    val targetPrice: Double = 0.0,
    @SerializedName("item_id")
    val itemId: String? = null,
    @SerializedName("enable_price_alert")
    val enablePriceAlert: Boolean = false,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("created_at")
    val createdAt: String, // ISO 8601 格式
    @SerializedName("updated_at")
    val updatedAt: String
) {
    fun toWishlistItem(): com.example.tradingplatform.data.wishlist.WishlistItem {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        val dateFormatWithMs = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US)
        val dateFormatWithZ = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        
        fun parseDate(dateStr: String): java.util.Date {
            return try {
                dateFormatWithZ.parse(dateStr) ?: dateFormatWithMs.parse(dateStr) ?: dateFormat.parse(dateStr) ?: java.util.Date()
            } catch (e: Exception) {
                java.util.Date()
            }
        }
        
        return com.example.tradingplatform.data.wishlist.WishlistItem(
            id = id,
            userId = userId,
            userEmail = userEmail,
            title = title,
            category = category ?: "",
            minPrice = minPrice,
            maxPrice = maxPrice,
            targetPrice = targetPrice,
            itemId = itemId ?: "",
            enablePriceAlert = enablePriceAlert,
            description = description ?: "",
            createdAt = parseDate(createdAt),
            updatedAt = parseDate(updatedAt)
        )
    }
    
    companion object {
        fun fromWishlistItem(item: com.example.tradingplatform.data.wishlist.WishlistItem): CreateWishlistItemRequest {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            return CreateWishlistItemRequest(
                id = if (item.id.isNotEmpty()) item.id else null,
                userId = item.userId,
                userEmail = item.userEmail,
                title = item.title,
                category = item.category.ifEmpty { null },
                minPrice = item.minPrice,
                maxPrice = item.maxPrice,
                targetPrice = item.targetPrice,
                itemId = item.itemId.ifEmpty { null },
                enablePriceAlert = item.enablePriceAlert,
                description = item.description.ifEmpty { null }
            )
        }
    }
}


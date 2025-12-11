package com.example.tradingplatform.data.items

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.tradingplatform.data.auth.AuthRepository
import com.example.tradingplatform.data.local.AppDatabase
import com.example.tradingplatform.data.local.ItemDao
import com.example.tradingplatform.data.local.ItemEntity
import com.example.tradingplatform.data.supabase.SupabaseApi
import com.example.tradingplatform.data.supabase.SupabaseClient
import com.example.tradingplatform.data.supabase.SupabaseItem
import com.example.tradingplatform.data.supabase.SupabaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Date
import java.util.UUID

data class Item(
    val id: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val category: String = "", // 商品类别 / Item category
    val story: String = "", // 商品故事/背景 / Item story/background
    val imageUrl: String = "", // 本地文件路径 / Local file path
    val phoneNumber: String = "",
    val ownerUid: String = "",
    val ownerEmail: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

class ItemRepository(
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "ItemRepository"
    }

    private val database: AppDatabase? = context?.let { AppDatabase.getDatabase(it) }
    private val itemDao: ItemDao? = database?.itemDao()
    private val authRepo: AuthRepository? = context?.let { AuthRepository(it) }
    private val supabaseApi: SupabaseApi? = context?.let { SupabaseClient.getApi() }
    private val supabaseStorage: SupabaseStorage? = context?.let { SupabaseStorage(it) }

    /**
     * 保存图片到本地存储 / Save image to local storage
     */
    private suspend fun saveImageToLocal(uri: Uri): String = withContext(Dispatchers.IO) {
        if (context == null) {
            throw IllegalStateException("Context 未初始化")
        }

        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            throw Exception("无法打开图片文件")
        }

        try {
            // 创建图片目录 / Create image directory
            val imagesDir = File(context.filesDir, "item_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            // 生成唯一文件名 / Generate unique filename
            val fileName = "item_${UUID.randomUUID()}.jpg"
            val file = File(imagesDir, fileName)

            // 复制文件 / Copy file
            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }

            Log.d(TAG, "图片保存成功: ${file.absolutePath}")
            file.absolutePath
        } finally {
            inputStream.close()
        }
    }

    /**
     * 添加商品 / Add item
     */
    suspend fun addItem(item: Item, imageUri: Uri? = null): String = withContext(Dispatchers.IO) {
        if (itemDao == null) {
            throw IllegalStateException("数据库未初始化")
        }

        // 获取当前用户信息 / Get current user information
        val currentEmail = authRepo?.getCurrentUserEmail() ?: "dev@example.com"
        val currentUid = authRepo?.getCurrentUserUid() ?: "dev_user_${System.currentTimeMillis()}"

        Log.d(TAG, "开始发布商品: ${item.title}")

        var imageUrl = item.imageUrl
        
        // 优先上传到 Supabase Storage，失败则保存到本地 / Prioritize uploading to Supabase Storage, fallback to local storage
        if (imageUri != null) {
            try {
                imageUrl = supabaseStorage?.uploadImage(imageUri) ?: saveImageToLocal(imageUri)
                Log.d(TAG, "图片上传完成: $imageUrl")
            } catch (e: Exception) {
                Log.e(TAG, "图片上传失败，尝试保存到本地", e)
                try {
                    imageUrl = saveImageToLocal(imageUri)
                    Log.d(TAG, "图片保存到本地: $imageUrl")
                } catch (localError: Exception) {
                    Log.e(TAG, "图片保存失败，继续发布商品（不包含图片）", localError)
                }
            }
        }

        val newItem = item.copy(
            id = UUID.randomUUID().toString(), // 使用 UUID 作为 ID / Use UUID as ID
            ownerUid = currentUid,
            ownerEmail = currentEmail,
            imageUrl = imageUrl,
            createdAt = Date(),
            updatedAt = Date()
        )

        // 保存到本地数据库 / Save to local database
        val itemEntity = ItemEntity.fromItem(newItem)
        itemDao.insertItem(itemEntity)
        Log.d(TAG, "商品已保存到本地数据库: ${newItem.id}")
        
        // 同步到 Supabase / Sync to Supabase
        try {
            if (supabaseApi == null) {
                Log.w(TAG, "Supabase API 未初始化，跳过同步")
            } else {
                val supabaseItemData = SupabaseItem.fromItem(newItem)
                Log.d(TAG, "准备同步到 Supabase: ${supabaseItemData}")
                val response = supabaseApi.createItem(supabaseItemData)
                if (response.isSuccessful) {
                    Log.d(TAG, "商品已同步到 Supabase: ${newItem.id}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Supabase 同步失败: HTTP ${response.code()} - $errorBody")
                    Log.e(TAG, "请求数据: $supabaseItemData")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase 同步异常（商品仍在本地）", e)
            Log.e(TAG, "异常类型: ${e.javaClass.simpleName}")
            Log.e(TAG, "异常消息: ${e.message}")
            Log.e(TAG, "异常堆栈:", e)
            e.printStackTrace()
            // 即使 Supabase 失败，本地数据仍然保存 / Even if Supabase fails, local data is still saved
        }
        
        newItem.id
    }

    /**
     * 更新商品价格 / Update item price
     */
    suspend fun updateItemPrice(itemId: String, newPrice: Double) = withContext(Dispatchers.IO) {
        if (itemDao == null) {
            throw IllegalStateException("数据库未初始化")
        }

        // 获取当前用户信息 / Get current user information
        val currentEmail = authRepo?.getCurrentUserEmail()
        val currentUid = authRepo?.getCurrentUserUid()

        // 获取现有商品 / Get existing item
        val existingEntity = itemDao.getItemById(itemId)
            ?: throw IllegalStateException("商品不存在")

        // 检查是否为商品所有者 / Check if user is the owner
        val isOwner = (currentUid != null && existingEntity.ownerUid == currentUid) ||
                (currentEmail != null && existingEntity.ownerEmail.equals(currentEmail, ignoreCase = true))

        if (!isOwner) {
            throw IllegalStateException("只有商品所有者可以修改价格")
        }

        val oldPrice = existingEntity.price
        Log.d(TAG, "更新商品价格: $itemId, 旧价格: $oldPrice, 新价格: $newPrice")

        // 更新本地数据库 / Update local database
        val updatedEntity = existingEntity.copy(
            price = newPrice,
            updatedAt = System.currentTimeMillis()
        )
        itemDao.insertItem(updatedEntity)
        Log.d(TAG, "商品价格已更新到本地数据库: $itemId")

        // 同步到 Supabase / Sync to Supabase
        try {
            if (supabaseApi != null) {
                val updateRequest = com.example.tradingplatform.data.supabase.UpdateItemRequest(
                    price = newPrice
                )
                val response = supabaseApi.updateItem("eq.$itemId", updateRequest)
                if (response.isSuccessful) {
                    Log.d(TAG, "商品价格已同步到 Supabase: $itemId")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.w(TAG, "Supabase 同步失败: ${response.code()} - $errorBody")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Supabase 同步失败（本地已更新）", e)
        }

        // 如果价格降低，检查并发送降价提醒 / If price dropped, check and send price alerts
        if (newPrice < oldPrice && context != null) {
            try {
                Log.d(TAG, "检测到降价，检查降价提醒...")
                val updatedItem = updatedEntity.toItem()
                checkAndSendPriceAlerts(updatedItem)
            } catch (e: Exception) {
                Log.e(TAG, "检查降价提醒失败", e)
            }
        }
    }

    /**
     * 检查并发送降价提醒给关注此商品的用户 / Check and send price alerts to users watching this item
     */
    private suspend fun checkAndSendPriceAlerts(item: Item) = withContext(Dispatchers.IO) {
        if (context == null) return@withContext
        
        try {
            val priceAlertService = com.example.tradingplatform.data.wishlist.PriceAlertService(context)
            
            // 从 Supabase 获取所有愿望清单（包括其他用户的）/ Get all wishlists from Supabase (including other users)
            val allWishlistItems = mutableListOf<com.example.tradingplatform.data.wishlist.WishlistItem>()
            
            try {
                val response = supabaseApi?.getAllWishlistItems(limit = 500)
                if (response?.isSuccessful == true) {
                    val supabaseItems = response.body() ?: emptyList()
                    allWishlistItems.addAll(supabaseItems.map { it.toWishlistItem() })
                    Log.d(TAG, "从 Supabase 获取 ${allWishlistItems.size} 个愿望清单项")
                }
            } catch (e: Exception) {
                Log.w(TAG, "从 Supabase 获取愿望清单失败", e)
            }
            
            // 筛选出关注此商品且启用了降价提醒的愿望清单项 / Filter wishlist items watching this item with price alerts enabled
            val relevantWishlistItems = allWishlistItems.filter { wish ->
                if (!wish.enablePriceAlert || wish.targetPrice <= 0) {
                    return@filter false
                }
                
                // 检查是否关联了此商品 / Check if linked to this item
                if (wish.itemId == item.id) {
                    return@filter item.price <= wish.targetPrice
                }
                
                // 检查标题和类别是否匹配 / Check if title and category match
                val titleMatch = item.title.contains(wish.title, ignoreCase = true) ||
                        wish.title.contains(item.title, ignoreCase = true)
                val categoryMatch = wish.category.isEmpty() || item.category == wish.category
                val priceMatch = item.price <= wish.targetPrice
                
                titleMatch && categoryMatch && priceMatch
            }
            
            Log.d(TAG, "找到 ${relevantWishlistItems.size} 个符合降价提醒条件的愿望清单项")
            
            // 发送降价提醒 / Send price alerts
            relevantWishlistItems.forEach { wish ->
                priceAlertService.sendPriceAlertForItem(wish, item)
            }
        } catch (e: Exception) {
            Log.e(TAG, "检查降价提醒失败", e)
        }
    }

    /**
     * 删除商品 / Delete item
     */
    suspend fun deleteItem(itemId: String) = withContext(Dispatchers.IO) {
        if (itemDao == null) {
            throw IllegalStateException("数据库未初始化")
        }

        // 检查是否为开发者模式（未登录）/ Check if developer mode (not logged in)
        val isDevMode = authRepo?.isLoggedIn() != true

        if (!isDevMode) {
            throw IllegalStateException("只有开发者模式可以删除商品")
        }

        Log.d(TAG, "删除商品: $itemId (开发者模式)")

        // 获取商品信息，删除关联的图片文件 / Get item info, delete associated image file
        val itemEntity = itemDao.getItemById(itemId)
        if (itemEntity != null && itemEntity.imageUrl.isNotEmpty()) {
            try {
                val imageFile = File(itemEntity.imageUrl)
                if (imageFile.exists()) {
                    imageFile.delete()
                    Log.d(TAG, "删除图片文件: ${itemEntity.imageUrl}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "删除图片文件失败", e)
            }
        }

        // 从本地数据库删除 / Delete from local database
        itemDao.deleteItemById(itemId)
        Log.d(TAG, "商品已从本地数据库删除: $itemId")
        
        // 从 Supabase 删除 / Delete from Supabase
        try {
            val response = supabaseApi?.deleteItem("eq.$itemId")
            if (response?.isSuccessful == true) {
                Log.d(TAG, "商品已从 Supabase 删除: $itemId")
            } else {
                val errorBody = response?.errorBody()?.string()
                Log.w(TAG, "Supabase 删除失败: ${response?.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Supabase 删除失败（商品已从本地删除）", e)
        }
    }

    /**
     * 获取商品列表（按创建时间倒序）/ Get item list (sorted by creation time descending)
     * 优先从 Supabase 获取，失败则从本地获取 / Prioritize Supabase, fallback to local
     */
    suspend fun listItems(limit: Int = 50): List<Item> = withContext(Dispatchers.IO) {
        // 尝试从 Supabase 获取 / Try to get from Supabase
        try {
            val response = supabaseApi?.getItems(limit = limit)
            if (response?.isSuccessful == true) {
                val supabaseItems = response.body() ?: emptyList()
                val items = supabaseItems.map { it.toItem() }
                
                // 同步到本地数据库 / Sync to local database
                items.forEach { item ->
                    try {
                        val entity = ItemEntity.fromItem(item)
                        itemDao?.insertItem(entity)
                    } catch (e: Exception) {
                        Log.w(TAG, "同步商品到本地失败: ${item.id}", e)
                    }
                }
                
                Log.d(TAG, "从 Supabase 获取 ${items.size} 个商品")
                return@withContext items
            } else {
                val errorBody = response?.errorBody()?.string()
                Log.w(TAG, "从 Supabase 获取商品失败: HTTP ${response?.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.w(TAG, "从 Supabase 获取商品失败，使用本地数据", e)
            Log.w(TAG, "异常类型: ${e.javaClass.simpleName}")
            Log.w(TAG, "异常消息: ${e.message}")
            e.printStackTrace()
        }
        
        // 从本地数据库获取 / Get from local database
        if (itemDao == null) {
            return@withContext emptyList()
        }

        val entities = itemDao.getItems(limit)
        val localItems = entities.map { it.toItem() }
        Log.d(TAG, "从本地数据库获取 ${localItems.size} 个商品")
        localItems
    }

    /**
     * 获取所有商品（不限制数量）/ Get all items (no limit)
     * 用于"我的"页面，确保获取用户的所有商品 / For "My" page, ensure all user's items are fetched
     */
    suspend fun listAllItems(): List<Item> = withContext(Dispatchers.IO) {
        // 尝试从 Supabase 获取 / Try to get from Supabase
        try {
            val response = supabaseApi?.getAllItems()
            if (response?.isSuccessful == true) {
                val supabaseItems = response.body() ?: emptyList()
                val items = supabaseItems.map { it.toItem() }
                
                // 同步到本地数据库 / Sync to local database
                items.forEach { item ->
                    try {
                        val entity = ItemEntity.fromItem(item)
                        itemDao?.insertItem(entity)
                    } catch (e: Exception) {
                        Log.w(TAG, "同步商品到本地失败: ${item.id}", e)
                    }
                }
                
                Log.d(TAG, "从 Supabase 获取所有商品: ${items.size} 个")
                return@withContext items
            } else {
                val errorBody = response?.errorBody()?.string()
                Log.w(TAG, "从 Supabase 获取所有商品失败: HTTP ${response?.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.w(TAG, "从 Supabase 获取所有商品失败，使用本地数据", e)
        }
        
        // 从本地数据库获取所有 / Get all from local database
        if (itemDao == null) {
            return@withContext emptyList()
        }

        val entities = itemDao.getAllItemsSync()
        val localItems = entities.map { it.toItem() }
        Log.d(TAG, "从本地数据库获取所有商品: ${localItems.size} 个")
        localItems
    }

    /**
     * 根据ID获取商品 / Get item by ID
     * 优先从 Supabase 获取，失败则从本地获取 / Prioritize Supabase, fallback to local
     */
    suspend fun getItemById(itemId: String): Item? = withContext(Dispatchers.IO) {
        // 尝试从 Supabase 获取 / Try to get from Supabase
        try {
            // Supabase PostgREST 使用 id=eq.{value} 格式 / Supabase PostgREST uses id=eq.{value} format
            val response = supabaseApi?.getItemById("eq.$itemId")
            if (response?.isSuccessful == true) {
                val supabaseItems = response.body() ?: emptyList()
                if (supabaseItems.isNotEmpty()) {
                    val item = supabaseItems.first().toItem()
                    // 同步到本地 / Sync to local
                    try {
                        val entity = ItemEntity.fromItem(item)
                        itemDao?.insertItem(entity)
                    } catch (e: Exception) {
                        Log.w(TAG, "同步商品到本地失败: ${item.id}", e)
                    }
                    return@withContext item
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "从 Supabase 获取商品失败，使用本地数据", e)
        }
        
        // 从本地数据库获取 / Get from local database
        if (itemDao == null) {
            return@withContext null
        }

        val entity = itemDao.getItemById(itemId)
        entity?.toItem()
    }

    /**
     * 获取商品列表 Flow（用于实时更新）/ Get item list Flow (for real-time updates)
     */
    fun getItemsFlow(): kotlinx.coroutines.flow.Flow<List<Item>> {
        if (itemDao == null) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }

        return itemDao.getAllItems().map { entities ->
            entities.map { it.toItem() }
        }
    }
}

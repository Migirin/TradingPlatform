package com.example.tradingplatform.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 商品数据访问对象
 */
@Dao
interface ItemDao {
    /**
     * 获取所有商品（按创建时间倒序）
     */
    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    /**
     * 获取所有商品（非 Flow，用于一次性查询）
     */
    @Query("SELECT * FROM items ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getItems(limit: Int = 50): List<ItemEntity>

    /**
     * 根据ID获取商品
     */
    @Query("SELECT * FROM items WHERE id = :itemId LIMIT 1")
    suspend fun getItemById(itemId: String): ItemEntity?

    /**
     * 插入商品
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    /**
     * 更新商品
     */
    @Update
    suspend fun updateItem(item: ItemEntity)

    /**
     * 删除商品
     */
    @Delete
    suspend fun deleteItem(item: ItemEntity)

    /**
     * 根据ID删除商品
     */
    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String)

    /**
     * 删除所有商品
     */
    @Query("DELETE FROM items")
    suspend fun deleteAllItems()
}






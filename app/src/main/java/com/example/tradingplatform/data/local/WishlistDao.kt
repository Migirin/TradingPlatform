package com.example.tradingplatform.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 愿望清单数据访问对象
 */
@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist WHERE user_id = :userId ORDER BY created_at DESC")
    fun getWishlistByUser(userId: String): Flow<List<WishlistEntity>>

    @Query("SELECT * FROM wishlist WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getWishlistByUserSync(userId: String): List<WishlistEntity>

    @Query("SELECT * FROM wishlist WHERE id = :id")
    suspend fun getWishlistItemById(id: String): WishlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistEntity)

    @Delete
    suspend fun deleteWishlistItem(item: WishlistEntity)

    @Query("DELETE FROM wishlist WHERE id = :id")
    suspend fun deleteWishlistItemById(id: String)

    @Query("SELECT * FROM wishlist ORDER BY created_at DESC")
    suspend fun getAllWishlistItems(): List<WishlistEntity>
}




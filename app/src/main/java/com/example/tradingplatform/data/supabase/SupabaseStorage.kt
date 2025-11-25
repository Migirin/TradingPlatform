package com.example.tradingplatform.data.supabase

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Supabase Storage 服务
 * 用于上传图片到 Supabase Storage
 */
class SupabaseStorage(private val context: Context) {
    companion object {
        private const val TAG = "SupabaseStorage"
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS) // 上传可能需要更长时间
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    
    /**
     * 上传图片到 Supabase Storage
     */
    suspend fun uploadImage(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            // 先保存到临时文件
            val tempFile = saveToTempFile(uri)
            
            // 生成唯一文件名
            val fileName = "item_${UUID.randomUUID()}.jpg"
            
            // 上传到 Supabase Storage
            val publicUrl = uploadToSupabase(tempFile, fileName)
            
            // 删除临时文件
            tempFile.delete()
            
            Log.d(TAG, "图片上传成功: $publicUrl")
            publicUrl
        } catch (e: Exception) {
            Log.e(TAG, "图片上传失败", e)
            throw Exception("图片上传失败: ${e.message ?: "未知错误"}")
        }
    }
    
    /**
     * 保存 URI 到临时文件
     */
    private fun saveToTempFile(uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            throw Exception("无法打开图片文件")
        }
        
        val tempFile = File(context.cacheDir, "temp_${UUID.randomUUID()}.jpg")
        try {
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
        } finally {
            inputStream.close()
        }
        return tempFile
    }
    
    /**
     * 上传文件到 Supabase Storage
     */
    private suspend fun uploadToSupabase(file: File, fileName: String): String = withContext(Dispatchers.IO) {
        val mediaType = "image/jpeg".toMediaType()
        val requestBody = file.asRequestBody(mediaType)
        
        val request = Request.Builder()
            .url("${SupabaseConfig.PROJECT_URL}/storage/v1/object/${SupabaseConfig.STORAGE_BUCKET_IMAGES}/$fileName")
            .header("apikey", SupabaseConfig.ANON_KEY)
            .header("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
            .header("Content-Type", "image/jpeg")
            .put(requestBody) // Supabase Storage 使用 PUT 方法
            .build()
        
        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            val errorBody = response.body?.string()
            Log.e(TAG, "上传失败: ${response.code} - $errorBody")
            throw Exception("上传失败: ${response.code} - $errorBody")
        }
        
        // 返回公开 URL
        "${SupabaseConfig.PROJECT_URL}/storage/v1/object/public/${SupabaseConfig.STORAGE_BUCKET_IMAGES}/$fileName"
    }
}


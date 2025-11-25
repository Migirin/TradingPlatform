package com.example.tradingplatform.data.supabase

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Supabase REST API 客户端
 * 使用 Retrofit 调用 Supabase PostgREST API
 */
object SupabaseClient {
    private const val TAG = "SupabaseClient"
    private var api: SupabaseApi? = null
    
    fun getApi(): SupabaseApi {
        if (api == null) {
            // 创建请求头拦截器，统一添加 API key
            val headerInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                
                // 确保 API key 没有多余的空格
                val apiKey = SupabaseConfig.ANON_KEY.trim()
                
                val newRequest = originalRequest.newBuilder()
                    .removeHeader("apikey") // 移除可能存在的旧头
                    .removeHeader("Authorization") // 移除可能存在的旧头
                    .addHeader("apikey", apiKey)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build()
                
                Log.d(TAG, "请求 URL: ${newRequest.url}")
                Log.d(TAG, "API Key 长度: ${apiKey.length}")
                Log.d(TAG, "API Key (前20字符): ${apiKey.take(20)}...")
                Log.d(TAG, "API Key (后20字符): ...${apiKey.takeLast(20)}")
                Log.d(TAG, "请求头 apikey: ${newRequest.header("apikey")?.take(20)}...")
                Log.d(TAG, "请求头 Authorization: ${newRequest.header("Authorization")?.take(30)}...")
                
                chain.proceed(newRequest)
            }
            
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(headerInterceptor)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .addInterceptor { chain ->
                    try {
                        val response = chain.proceed(chain.request())
                        if (!response.isSuccessful) {
                            Log.e(TAG, "HTTP 错误: ${response.code} ${response.message}")
                            val errorBody = response.peekBody(1024).string()
                            Log.e(TAG, "错误响应: $errorBody")
                        }
                        response
                    } catch (e: Exception) {
                        Log.e(TAG, "网络请求异常", e)
                        throw e
                    }
                }
                .build()
            
            // Retrofit baseUrl 必须以 / 结尾，且路径不能以 / 开头
            val baseUrl = "${SupabaseConfig.PROJECT_URL}/rest/v1/"
            Log.d(TAG, "Retrofit baseUrl: $baseUrl")
            
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            api = retrofit.create(SupabaseApi::class.java)
        }
        return api!!
    }
}


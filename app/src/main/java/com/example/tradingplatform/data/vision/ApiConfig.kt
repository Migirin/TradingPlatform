package com.example.tradingplatform.data.vision

/**
 * 第三方 API 配置
 * 
 * 使用方式：
 * 1. 在 local.properties 中添加 API 密钥（不要提交到 Git）
 * 2. 或者在这里直接配置（仅用于开发测试）
 */
object ApiConfig {
    // 阿里云配置
    object Aliyun {
        // 从 local.properties 读取，如果没有则使用默认值（需要替换）
        val ACCESS_KEY_ID: String = getProperty("aliyun.access.key.id", "")
        val ACCESS_KEY_SECRET: String = getProperty("aliyun.access.key.secret", "")
        val ENDPOINT: String = "https://imageseg.cn-shanghai.aliyuncs.com" // 商品识别服务端点
    }

    // 腾讯云配置
    object Tencent {
        val SECRET_ID: String = getProperty("tencent.secret.id", "")
        val SECRET_KEY: String = getProperty("tencent.secret.key", "")
        val REGION: String = "ap-shanghai"
    }

    // 百度 AI 配置
    object Baidu {
        val API_KEY: String = getProperty("baidu.api.key", "d76xxC7rMeaART7el5Ldv4hK")
        val SECRET_KEY: String = getProperty("baidu.secret.key", "ToJsSJqSTqkz7NN9LzKsQelHVJnljpQn")
    }

    /**
     * 从 local.properties 读取配置
     */
    private fun getProperty(key: String, defaultValue: String): String {
        return try {
            val properties = java.util.Properties()
            val file = java.io.File("local.properties")
            if (file.exists()) {
                file.inputStream().use { properties.load(it) }
                properties.getProperty(key, defaultValue)
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            defaultValue
        }
    }

    /**
     * 检查配置是否完整
     */
    fun isAliyunConfigured(): Boolean {
        return Aliyun.ACCESS_KEY_ID.isNotEmpty() && Aliyun.ACCESS_KEY_SECRET.isNotEmpty()
    }

    fun isTencentConfigured(): Boolean {
        return Tencent.SECRET_ID.isNotEmpty() && Tencent.SECRET_KEY.isNotEmpty()
    }

    fun isBaiduConfigured(): Boolean {
        return Baidu.API_KEY.isNotEmpty() && Baidu.SECRET_KEY.isNotEmpty()
    }
    
    /**
     * 检查 ML Kit 云端是否可用
     * 注意：ML Kit 云端需要 Firebase 配置，当前实现不支持
     */
    fun isMlKitCloudAvailable(): Boolean {
        // ML Kit 云端需要 Firebase 配置，当前未实现
        // 如果需要使用，需要：
        // 1. 配置 Firebase 项目
        // 2. 启用 ML Kit API
        // 3. 使用云端模型选项
        return false
    }
}


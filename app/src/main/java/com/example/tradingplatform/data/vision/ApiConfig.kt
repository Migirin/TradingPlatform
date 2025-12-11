package com.example.tradingplatform.data.vision

/**
 * 第三方 API 配置 / Third-party API Configuration
 * 
 * 使用方式 / Usage:
 * 1. 在 local.properties 中添加 API 密钥（不要提交到 Git）/ Add API keys in local.properties (do not commit to Git)
 * 2. 或者在这里直接配置（仅用于开发测试）/ Or configure directly here (for development testing only)
 */
object ApiConfig {
    // 阿里云配置 / Aliyun configuration
    object Aliyun {
        // 从 local.properties 读取，如果没有则使用默认值（需要替换）/ Read from local.properties, use default if not found (needs replacement)
        val ACCESS_KEY_ID: String = getProperty("aliyun.access.key.id", "")
        val ACCESS_KEY_SECRET: String = getProperty("aliyun.access.key.secret", "")
        val ENDPOINT: String = "https://imageseg.cn-shanghai.aliyuncs.com" // 商品识别服务端点 / Product recognition service endpoint
    }

    // 腾讯云配置 / Tencent Cloud configuration
    object Tencent {
        val SECRET_ID: String = getProperty("tencent.secret.id", "")
        val SECRET_KEY: String = getProperty("tencent.secret.key", "")
        val REGION: String = "ap-shanghai"
    }

    // 百度 AI 配置 / Baidu AI configuration
    object Baidu {
        val API_KEY: String = getProperty("baidu.api.key", "d76xxC7rMeaART7el5Ldv4hK")
        val SECRET_KEY: String = getProperty("baidu.secret.key", "ToJsSJqSTqkz7NN9LzKsQelHVJnljpQn")
    }

    // SendGrid 邮件服务配置 / SendGrid Email Service configuration
    // 注意：API Key 已直接配置在代码中，所有开发者可直接使用
    object SendGrid {
        // 直接使用配置的 API Key（所有开发者共享）
        // 如果需要在 local.properties 中覆盖，可以配置 sendgrid.api.key
        // 默认使用占位符，必须在 local.properties 中覆盖
        // 示例：sendgrid.api.key=YOUR_SENDGRID_API_KEY_HERE
        val API_KEY: String = getProperty("sendgrid.api.key", "YOUR_SENDGRID_API_KEY_HERE")
        val FROM_EMAIL: String = getProperty("sendgrid.from.email", "xinghang.cao@ucdconnect.ie")
        val FROM_NAME: String = "BJUT SecondHand"
    }

    /**
     * 从 local.properties 读取配置 / Read configuration from local.properties
     * 注意：Android 运行时无法直接读取项目根目录的 local.properties
     * 如果读取失败，会使用默认值（开发测试用）
     */
    private fun getProperty(key: String, defaultValue: String): String {
        return try {
            // 尝试从项目根目录读取（编译时）
            val properties = java.util.Properties()
            val projectRoot = System.getProperty("user.dir")
            val file = java.io.File(projectRoot, "local.properties")
            
            if (file.exists()) {
                file.inputStream().use { properties.load(it) }
                val value = properties.getProperty(key, defaultValue)
                android.util.Log.d("ApiConfig", "从 local.properties 读取 $key: ${if (value.length > 10) value.take(10) + "..." else value}")
                value
            } else {
                // 如果找不到文件，尝试相对路径
                val relativeFile = java.io.File("local.properties")
                if (relativeFile.exists()) {
                    relativeFile.inputStream().use { properties.load(it) }
                    val value = properties.getProperty(key, defaultValue)
                    android.util.Log.d("ApiConfig", "从相对路径读取 $key: ${if (value.length > 10) value.take(10) + "..." else value}")
                    value
                } else {
                    android.util.Log.w("ApiConfig", "未找到 local.properties，使用默认值: $key")
                    defaultValue
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ApiConfig", "读取配置失败 $key，使用默认值", e)
            defaultValue
        }
    }

    /**
     * 检查配置是否完整 / Check if configuration is complete
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

    fun isSendGridConfigured(): Boolean {
        return SendGrid.API_KEY.isNotEmpty() && SendGrid.FROM_EMAIL.isNotEmpty()
    }
    
    /**
     * 检查 ML Kit 云端是否可用 / Check if ML Kit Cloud is available
     * 注意：ML Kit 云端需要 Firebase 配置，当前实现不支持 / Note: ML Kit Cloud requires Firebase configuration, current implementation does not support
     */
    fun isMlKitCloudAvailable(): Boolean {
        // ML Kit 云端需要 Firebase 配置，当前未实现 / ML Kit Cloud requires Firebase configuration, not currently implemented
        // 如果需要使用，需要：/ If you need to use it, you need to:
        // 1. 配置 Firebase 项目 / Configure Firebase project
        // 2. 启用 ML Kit API / Enable ML Kit API
        // 3. 使用云端模型选项 / Use cloud model options
        return false
    }
}


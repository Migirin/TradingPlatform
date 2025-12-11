package com.example.tradingplatform.data.items

import android.content.Context
import android.util.Log
import com.example.tradingplatform.data.local.AppDatabase
import com.example.tradingplatform.data.local.ItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

/**
 * Seed sample textbook items into the local database for demo and testing. / 将示例教材商品种子数据插入本地数据库，用于演示和测试
 */
object SampleItemSeeder {

    private const val TAG = "SampleItemSeeder"

    /**
     * Ensure there are some sample items in the local database. / 确保本地数据库中有一些示例商品
     * Only inserts data when the items table is empty. / 仅在商品表为空时插入数据
     */
    suspend fun ensureSampleItems(context: Context) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val dao = db.itemDao()

            val count = dao.getItemCount()
            if (count > 0) {
                Log.d(TAG, "Items already exist in database: $count, skip seeding")
                return@withContext
            }

            val now = Date().time
            val sampleItems = listOf(
                // ==== 通用基础教材（与多个课程匹配）==== / ==== Common basic textbooks (matching multiple courses) ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Calculus textbook (高等数学)",
                    price = 30.0,
                    description = "Second-hand calculus textbook, suitable for 高等数学 / Maths (Engineering) / BDIC1029J / BDIC1030J.",
                    category = "图书文具",
                    story = "Used for one semester, with some notes.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Linear Algebra textbook (线性代数)",
                    price = 28.0,
                    description = "Linear algebra textbook for 线性代数 / BDIC1014J / BDIC1044J.",
                    category = "图书文具",
                    story = "Clean copy with a few highlights.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Computer Networks textbook (计算机网络)",
                    price = 35.0,
                    description = "Textbook for 计算机网络 / Computer Networks related courses.",
                    category = "图书文具",
                    story = "Used only for exam preparation.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== SE 大一上 (term1, grade 1) ==== / ==== SE Year 1 Term 1 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大一上 高等数学（工）1 教材",
                    price = 32.0,
                    description = "Official textbook for SE year 1 term 1 course 高等数学（工）1 / Maths (Engineering) 1 (BDIC1029J).",
                    category = "图书文具",
                    story = "SE freshman calculus book with annotation notes.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大一上 线性代数 教材",
                    price = 29.0,
                    description = "Textbook for BDIC1014J 线性代数 / Linear Algebra (Engineering).",
                    category = "图书文具",
                    story = "Used in SE grade 1 term 1.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大一上 程序设计1 教材",
                    price = 33.0,
                    description = "Intro to Programming 1 (COMP1001J / COMP1004J) textbook for SE year1 term1.",
                    category = "图书文具",
                    story = "Covers basic C/Java programming examples.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== SE 大一下 (term2, grade 1) ==== / ==== SE Year 1 Term 2 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大一下 大学物理Ⅰ 教材",
                    price = 36.0,
                    description = "University Physics I textbook for SE grade1 term2 (BDIC1015J / BDIC2008J).",
                    category = "图书文具",
                    story = "With formula summary and problem solutions.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大一下 学术英语 听说读写 教材",
                    price = 24.0,
                    description = "Academic English textbooks for 学术英语-口语/听力/阅读/写作 (College English 1-4).",
                    category = "图书文具",
                    story = "Bundle of four College English books.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大一下 爱尔兰文化 参考书",
                    price = 20.0,
                    description = "Reading materials for ARCH1001J 爱尔兰文化 (Irish Culture).",
                    category = "图书文具",
                    story = "Contains essays and case studies about Irish culture.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== SE 大二上 (grade 2 term1) ==== / ==== SE Year 2 Term 1 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大二上 概率论与数理统计 教材",
                    price = 31.0,
                    description = "Textbook for BDIC2005J 概率论与数理统计.",
                    category = "图书文具",
                    story = "Includes exercises for probability and statistics.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大二上 数据结构与算法 教材",
                    price = 34.0,
                    description = "Textbook for COMP2010J 数据结构与算法.",
                    category = "图书文具",
                    story = "Classic DS & Algorithms book (C/Java examples).",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大二上 面向对象编程 教材",
                    price = 33.0,
                    description = "Textbook for COMP2011J 面向对象编程.",
                    category = "图书文具",
                    story = "Covers OOP concepts and design patterns.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== SE 大二下 (grade 2 term2) ==== / ==== SE Year 2 Term 2 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大二下 操作系统 教材",
                    price = 36.0,
                    description = "Textbook for COMP2012J 操作系统.",
                    category = "图书文具",
                    story = "Includes process, memory and file system chapters.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大二下 大学物理Ⅱ 习题册",
                    price = 18.0,
                    description = "Exercise book for BDIC2008J 大学物理Ⅰ-2.",
                    category = "图书文具",
                    story = "All problems solved, perfect for exam prep.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大二下 学术写作与沟通技巧 读本",
                    price = 22.0,
                    description = "Reading pack for BDIC2015J 学术写作与沟通技巧.",
                    category = "图书文具",
                    story = "Marked key phrases and templates.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== SE 大三上 (grade 3 term1) ==== / ==== SE Year 3 Term 1 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大三上 软件工程方法学 教材",
                    price = 38.0,
                    description = "Textbook for BDIC3023J 软件工程方法学.",
                    category = "图书文具",
                    story = "Contains UML, requirements and testing chapters.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大三上 分布式系统 教材",
                    price = 39.0,
                    description = "Textbook for COMP3008J 分布式系统.",
                    category = "图书文具",
                    story = "Covers RPC, consistency and fault tolerance.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大三上 移动计算 教材",
                    price = 37.0,
                    description = "Textbook for COMP3011J 移动计算, used in this course demo.",
                    category = "图书文具",
                    story = "Includes Android development examples.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== SE 大三下 (grade 3 term2) ==== / ==== SE Year 3 Term 2 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大三下 计算机图形学 教材",
                    price = 40.0,
                    description = "Textbook for COMP3033J 计算机图形学.",
                    category = "图书文具",
                    story = "Good condition, with OpenGL examples.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大三下 多范式编程 教材",
                    price = 34.0,
                    description = "Textbook for COMP3038J 多范式编程.",
                    category = "图书文具",
                    story = "Functional & logic programming examples inside.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大三下 高级软件技术 笔记本",
                    price = 15.0,
                    description = "Lecture notes and printouts for COMP3039J 高级软件技术 1.",
                    category = "图书文具",
                    story = "Handwritten notes covering advanced topics.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== SE 大四上 (grade 4 term1) ==== / ==== SE Year 4 Term 1 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大四上 设计模式 教材",
                    price = 38.0,
                    description = "Textbook for BDIC3024J 设计模式.",
                    category = "图书文具",
                    story = "Covers GoF design patterns with Java examples.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大四上 安全与隐私 教材",
                    price = 36.0,
                    description = "Textbook for BDIC3025J 安全与隐私.",
                    category = "图书文具",
                    story = "Contains crypto and privacy case studies.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大四上 机器学习 教材",
                    price = 42.0,
                    description = "Textbook for COMP3010J 机器学习.",
                    category = "图书文具",
                    story = "Includes exercises on regression, SVM and neural networks.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== SE 大四下 (grade 4 term2) ==== / ==== SE Year 4 Term 2 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大四下 计算机系统性能 教材",
                    price = 37.0,
                    description = "Textbook for COMP3014J 计算机系统性能.",
                    category = "图书文具",
                    story = "Performance analysis and benchmarking techniques.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大四下 增强与虚拟现实 教材",
                    price = 40.0,
                    description = "Textbook for COMP3025J 增强虚拟现实.",
                    category = "图书文具",
                    story = "Includes AR/VR concepts and Unity examples.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "SE 大四下 高级软件技术2 资料包",
                    price = 20.0,
                    description = "Printouts and slides for advanced software technology courses.",
                    category = "图书文具",
                    story = "Useful for SE capstone project revision.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== FIN 大一上 (FIN grade1 term1) ==== / ==== FIN Year 1 Term 1 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大一上 高等数学1 教材",
                    price = 30.0,
                    description = "Textbook for BDIC1040J/BDIC1041J 高等数学1/2.",
                    category = "图书文具",
                    story = "Used for FIN freshman calculus.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大一上 线性代数 教材",
                    price = 26.0,
                    description = "Textbook for BDIC1044J 线性代数.",
                    category = "图书文具",
                    story = "Suitable for freshmen in Finance major.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大一上 经济学导论 教材",
                    price = 32.0,
                    description = "Introductory textbook for BDIC1045J 经济学导论.",
                    category = "图书文具",
                    story = "Covers basic micro and macro economics.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== FIN 大一下 (grade1 term2) ==== / ==== FIN Year 1 Term 2 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大一下 爱尔兰文化 读本",
                    price = 18.0,
                    description = "Reading materials for ARCH1001J 爱尔兰文化.",
                    category = "图书文具",
                    story = "Used for term papers and presentations.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大一下 体育-2 装备包",
                    price = 80.0,
                    description = "Sports kit for BDIC1007J 体育-2 (jersey, shorts).",
                    category = "运动户外",
                    story = "Lightly used, clean.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大一下 思政课程 资料合订本",
                    price = 15.0,
                    description = "Printouts and notes for 思想道德与法治 / 国家安全教育 / 形势与政策2.",
                    category = "图书文具",
                    story = "Useful for political theory exams.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== FIN 大二上 (grade2 term1) ==== / ==== FIN Year 2 Term 1 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大二上 财务会计学导论 教材",
                    price = 34.0,
                    description = "Textbook for BDIC1039J 财务会计学导论.",
                    category = "图书文具",
                    story = "Contains many worked accounting examples.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大二上 中级微观经济学1 教材",
                    price = 38.0,
                    description = "Textbook for BDIC2020J 中级微观经济学1.",
                    category = "图书文具",
                    story = "Covers consumer and producer theory in depth.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大二上 金融数据学 教材",
                    price = 36.0,
                    description = "Textbook for BDIC2026J 金融数据学.",
                    category = "图书文具",
                    story = "Focus on data analysis in finance.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== FIN 大二下 (grade2 term2) ==== / ==== FIN Year 2 Term 2 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大二下 宏观经济学原理 教材",
                    price = 33.0,
                    description = "Textbook for ECON1001J 宏观经济学原理.",
                    category = "图书文具",
                    story = "Covers GDP, inflation and monetary policy.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大二下 学术写作与沟通技巧 读本",
                    price = 20.0,
                    description = "Reading pack for BDIC2015J 学术写作与沟通技巧 (Finance).",
                    category = "图书文具",
                    story = "Marked useful writing templates.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大二下 概率论与数理统计 习题册",
                    price = 18.0,
                    description = "Exercise book for BDIC2005J 概率论与数理统计 (Finance).",
                    category = "图书文具",
                    story = "Almost all exercises completed, great for review.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== FIN 大三上 (grade3 term1) ==== / ==== FIN Year 3 Term 1 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大三上 博弈论 教材",
                    price = 38.0,
                    description = "Textbook for ECON2004J 博弈论.",
                    category = "图书文具",
                    story = "Contains many real-world game theory cases.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大三上 国际货币银行学 教材",
                    price = 40.0,
                    description = "Textbook for ECON3001J 国际货币银行学.",
                    category = "图书文具",
                    story = "Good condition, with FX and banking chapters highlighted.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大三上 金融经济学：资产定价 教材",
                    price = 42.0,
                    description = "Textbook for ECON3006J 金融经济学：资产定价.",
                    category = "图书文具",
                    story = "Contains CAPM and option pricing chapters.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== FIN 大三下 (grade3 term2) ==== / ==== FIN Year 3 Term 2 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大三下 线性建模 教材",
                    price = 34.0,
                    description = "Textbook for STAT2004J 线性建模.",
                    category = "图书文具",
                    story = "Used for regression and forecasting modules.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大三下 预测分析 教材",
                    price = 36.0,
                    description = "Textbook for STAT2005J 预测分析.",
                    category = "图书文具",
                    story = "Contains case studies on time-series forecasting.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大三下 企业金融经济 教材",
                    price = 38.0,
                    description = "Textbook for ECON3004J 企业金融经济.",
                    category = "图书文具",
                    story = "Corporate finance and investment case studies.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== FIN 大四上 (grade4 term1) ==== / ==== FIN Year 4 Term 1 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大四上 国际金融管理 教材",
                    price = 40.0,
                    description = "Textbook for BDIC3009J 国际金融管理.",
                    category = "图书文具",
                    story = "Used for final year international finance course.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大四上 行为金融学 教材",
                    price = 39.0,
                    description = "Textbook for BDIC3016J 行为金融学.",
                    category = "图书文具",
                    story = "Behavioural finance cases and experiments.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大四上 金融危机：历史与经济原理 教材",
                    price = 41.0,
                    description = "Textbook for ECON3020J 金融危机：历史与经济原理.",
                    category = "图书文具",
                    story = "Covers major financial crises and theory.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== FIN 大四下 (grade4 term2) ==== / ==== FIN Year 4 Term 2 ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大四下 发展经济学 教材",
                    price = 35.0,
                    description = "Textbook for ECON3028J 发展经济学.",
                    category = "图书文具",
                    story = "Contains development case studies.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大四下 时间序列 教材",
                    price = 38.0,
                    description = "Textbook for STAT3007J 时间序列.",
                    category = "图书文具",
                    story = "ARIMA and GARCH models in detail.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "FIN 大四下 产业经济学 教材",
                    price = 36.0,
                    description = "Textbook for ECON3022J 产业经济学.",
                    category = "图书文具",
                    story = "Industrial organization and competition policy.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),

                // ==== 四六级相关教材 / 资料 / 耳机 demo ==== / ==== CET-4/6 related textbooks / materials / headset demo ====
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "英语四级真题合集 2020-2024 (CET-4)",
                    price = 25.0,
                    description = "四级 真题 集合，含 CET-4 past papers, vocabulary list and listening scripts.",
                    category = "图书文具",
                    story = "All listening scripts are marked, perfect for CET-4 exam prep.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "英语六级真题+听力训练 (CET-6)",
                    price = 27.0,
                    description = "六级 真题 册，包含 CET-6 listening practice and mock test.",
                    category = "图书文具",
                    story = "Used once, contains handwritten answers.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "CET-4 Vocabulary Book 英语四级词汇书",
                    price = 22.0,
                    description = "Compact CET-4 vocabulary book with word list and example sentences.",
                    category = "图书文具",
                    story = "Highlighted high-frequency 四级 words.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "CET-6 Vocabulary Book 英语六级词汇书",
                    price = 24.0,
                    description = "CET-6 vocabulary and mock test book with listening section.",
                    category = "图书文具",
                    story = "Helps for CET-6 vocabulary and reading.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                ),
                ItemEntity(
                    id = UUID.randomUUID().toString(),
                    title = "四六级备考降噪耳机 (listening headset)",
                    price = 120.0,
                    description = "Noise-cancelling headset ideal for 四级 / 六级 listening practice and English exam preparation.",
                    category = "电子产品",
                    story = "Used for CET-4 and CET-6 listening mock tests.",
                    imageUrl = "",
                    phoneNumber = "1234567890",
                    ownerUid = "seed_user",
                    ownerEmail = "seed@example.com",
                    createdAt = now,
                    updatedAt = now
                )
            )

            sampleItems.forEach { item ->
                dao.insertItem(item)
            }

            Log.d(TAG, "Seeded ${sampleItems.size} sample items into local database")
        }
    }
}

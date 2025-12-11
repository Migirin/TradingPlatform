package com.example.tradingplatform.data.timetable

import android.content.Context
import android.util.Log
import com.example.tradingplatform.data.auth.AuthRepository
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.data.items.ItemRepository
import com.example.tradingplatform.data.wishlist.WishlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

enum class RecommendationReasonType {
    CURRENT_TERM,
    PREVIEW_TERM,
    CET_SEASON
}

data class RecommendationReason(
    val type: RecommendationReasonType
)

data class RecommendedItem(
    val item: Item,
    val reasons: List<RecommendationReason>
)

/**
 * 基于课表和已发布商品的本地教材推荐仓库 / Local textbook recommendation repository based on timetable and posted items
 *
 * 此仓库完全在设备上运行。它使用学生的课表来查找相关课程，然后通过简单的关键词搜索在标题/描述/类别中匹配已发布的商品。 / This repository runs fully on device. It uses the student's timetable to find relevant courses and then matches posted items by simple keyword searching on title/description/category.
 */
class TextbookRecommendationRepository(
    private val context: Context? = null
) {

    companion object {
        private const val TAG = "TextbookRecRepo"
        private const val MAX_RESULTS = 20
    }

    private val timetableRepository: TimetableRepository? = context?.let { TimetableRepository(it) }
    private val itemRepository: ItemRepository? = context?.let { ItemRepository(it) }
    private val authRepository: AuthRepository? = context?.let { AuthRepository(it) }
    private val wishlistRepository: WishlistRepository? = context?.let { WishlistRepository(it) }
    private val cetKeywords: List<String> = listOf(
        // 中文关键词 / Chinese keywords
        "四级", "六级", "四六级", "英语四级", "英语六级", "真题", "词汇", "听力",
        // 英文关键词（存储为小写，匹配时也使用小写）/ English keywords (stored in lowercase, matched in lowercase)
        "cet-4", "cet4", "cet 4",
        "cet-6", "cet6", "cet 6",
        "college english test", "english exam", "english test",
        "vocabulary", "word list", "listening", "mock test"
    )

    private data class TermWeights(
        val term1Weight: Double,
        val term2Weight: Double
    )

    private data class ScoredItem(
        val item: Item,
        val score: Double,
        val reasons: List<RecommendationReason>
    )

    /**
     * 获取给定学号在特定日期的推荐商品 / Get recommended items for a given student ID at a specific date
     * 此方法用于本地演示和应用内推荐 / This is intended for local demo and in-app recommendation
     */
    suspend fun getRecommendedItemsForStudent(
        studentId: String,
        now: LocalDate = LocalDate.now()
    ): List<RecommendedItem> = withContext(Dispatchers.IO) {
        if (timetableRepository == null || itemRepository == null) {
            Log.w(TAG, "Repositories not initialized, return empty list")
            return@withContext emptyList()
        }

        return@withContext try {
            val effectiveNow = resolveEffectiveDate(now)
            // 1. 查询该学生的第一学期和第二学期课程 / Query both term-1 and term-2 courses for this student
            val year = effectiveNow.year
            val dateForTerm1 = LocalDate.of(year, 10, 1) // term=1
            val dateForTerm2 = LocalDate.of(year, 3, 1)  // term=2

            val term1Courses = timetableRepository.getCoursesForStudent(studentId, dateForTerm1)
            val term2Courses = timetableRepository.getCoursesForStudent(studentId, dateForTerm2)

            if (term1Courses.isEmpty() && term2Courses.isEmpty()) {
                Log.d(TAG, "No timetable courses found for student: $studentId")
                return@withContext emptyList()
            }

            // 2. 为每个学期和四六级构建关键词列表 / Build keyword lists for each term and CET
            val term1Keywords = buildKeywordsFromCourses(term1Courses)
            val term2Keywords = buildKeywordsFromCourses(term2Courses)

            val hasTermKeywords = term1Keywords.isNotEmpty() || term2Keywords.isNotEmpty()
            val cetSeason = isCetSeason(effectiveNow)

            if (!hasTermKeywords && !cetSeason) {
                Log.d(TAG, "No valid keywords built from courses and not CET season")
                return@withContext emptyList()
            }

            // 3. 加载商品（优先通过 ItemRepository 从远程获取）/ Load items (prefer remote via ItemRepository)
            val allItems = itemRepository.listItems(limit = 200)
            if (allItems.isEmpty()) {
                Log.d(TAG, "No items available for recommendation")
                return@withContext emptyList()
            }

            // 4. 过滤掉当前用户自己的商品 / Filter out current user's own items
            val currentUid = authRepository?.getCurrentUserUid()
            val currentEmail = authRepository?.getCurrentUserEmail()?.lowercase()
            val availableItems = allItems.filter { item ->
                val uidMatch = currentUid?.let { item.ownerUid == it } ?: false
                val emailMatch = currentEmail?.let {
                    item.ownerEmail.equals(it, ignoreCase = true)
                } ?: false
                !uidMatch && !emailMatch
            }

            if (availableItems.isEmpty()) {
                Log.d(TAG, "No other users' items available for recommendation")
                return@withContext emptyList()
            }

            // 5. 加载当前用户的愿望清单以进行个性化 / Load current user's wishlist for personalization
            val wishlistItems = try {
                wishlistRepository?.getWishlistSync().orEmpty()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load wishlist for personalization", e)
                emptyList()
            }

            val wishlistItemIds = wishlistItems
                .mapNotNull { it.itemId.takeIf { id -> id.isNotBlank() } }
                .toSet()

            val wishlistKeywords: List<String> = buildList {
                for (wish in wishlistItems) {
                    val title = wish.title.trim()
                    if (title.length >= 2) add(title)
                    val category = wish.category.trim()
                    if (category.length >= 2) add(category)
                    val desc = wish.description.trim()
                    if (desc.length >= 2) add(desc)
                }
            }

            // 6. 根据课表/四六级关键词和愿望清单个性化对商品进行评分 / Score items by timetable/CET keywords and wishlist personalization
            val weights = computeTermWeights(effectiveNow.monthValue)
            val scored = availableItems.map { item ->
                val term1Score = if (term1Keywords.isNotEmpty()) {
                    calculateMatchScore(item, term1Keywords)
                } else 0.0

                val term2Score = if (term2Keywords.isNotEmpty()) {
                    calculateMatchScore(item, term2Keywords)
                } else 0.0

                val cetScore = if (cetSeason) {
                    calculateMatchScore(item, cetKeywords)
                } else 0.0

                val baseScore = weights.term1Weight * term1Score +
                        weights.term2Weight * term2Score +
                        cetScore

                val wishlistDirectBoost = if (wishlistItemIds.contains(item.id)) 0.5 else 0.0
                val wishlistKeywordScore = if (wishlistKeywords.isNotEmpty()) {
                    calculateMatchScore(item, wishlistKeywords)
                } else 0.0

                val personalizationBoost = wishlistDirectBoost + 0.5 * wishlistKeywordScore
                val finalScore = baseScore + personalizationBoost

                val reasons = buildReasonsForScores(
                    term1Score = term1Score,
                    term2Score = term2Score,
                    cetScore = cetScore,
                    weights = weights,
                    cetSeason = cetSeason
                )

                ScoredItem(
                    item = item,
                    score = finalScore,
                    reasons = reasons
                )
            }.filter { it.score > 0.0 }

            if (scored.isEmpty()) {
                Log.d(TAG, "No items matched timetable keywords")
                return@withContext emptyList()
            }

            val sorted = scored.sortedByDescending { it.score }
            val result = sorted.take(MAX_RESULTS).map { scoredItem ->
                RecommendedItem(
                    item = scoredItem.item,
                    reasons = scoredItem.reasons
                )
            }
            Log.d(TAG, "Recommended ${result.size} items for student $studentId")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build recommendations", e)
            emptyList()
        }
    }

    private fun resolveEffectiveDate(now: LocalDate): LocalDate {
        val simulatedMonth = authRepository?.getSimulatedMonth()
        if (simulatedMonth == null || simulatedMonth !in 1..12) {
            return now
        }
        return try {
            now.withMonth(simulatedMonth)
        } catch (e: Exception) {
            Log.w(TAG, "Invalid simulated month: $simulatedMonth", e)
            now
        }
    }

    /**
     * 根据有效日历月份计算第一学期（term 1）和第二学期（term 2）课程的权重 / Compute weights for first-term (term 1) and second-term (term 2) courses based on the effective calendar month
     *
     * 规则：/ Rules:
     *  - 对于第二学期课程：/ For second term courses:
     *      - 1-2月：最高权重 / Jan-Feb (1-2): highest weight
     *      - 3-6月：中等权重 / Mar-Jun (3-6): medium weight
     *      - 其他月份（7-12）：最低权重 / Other months (7-12): lowest weight
     *  - 对于第一学期课程：/ For first term courses:
     *      - 7-8月：最高权重 / Jul-Aug (7-8): highest weight
     *      - 9-12月：中等权重 / Sep-Dec (9-12): medium weight
     *      - 其他月份（1-6）：最低权重 / Other months (1-6): lowest weight
     */
    private fun computeTermWeights(month: Int): TermWeights {
        val low = 0.3
        val mid = 0.6
        val high = 1.0

        val term2Weight = when (month) {
            in 1..2 -> high
            in 3..6 -> mid
            else -> low
        }

        val term1Weight = when (month) {
            in 7..8 -> high
            in 9..12 -> mid
            else -> low
        }

        return TermWeights(term1Weight = term1Weight, term2Weight = term2Weight)
    }

    private fun buildReasonsForScores(
        term1Score: Double,
        term2Score: Double,
        cetScore: Double,
        weights: TermWeights,
        cetSeason: Boolean
    ): List<RecommendationReason> {
        val reasons = mutableListOf<RecommendationReason>()
        val term1Primary = weights.term1Weight > weights.term2Weight

        if (term1Score > 0.0) {
            val type = if (term1Primary) {
                RecommendationReasonType.CURRENT_TERM
            } else {
                RecommendationReasonType.PREVIEW_TERM
            }
            reasons.add(RecommendationReason(type))
        }

        if (term2Score > 0.0) {
            val type = if (!term1Primary) {
                RecommendationReasonType.CURRENT_TERM
            } else {
                RecommendationReasonType.PREVIEW_TERM
            }
            if (reasons.none { it.type == type }) {
                reasons.add(RecommendationReason(type))
            }
        }

        if (cetSeason && cetScore > 0.0) {
            if (reasons.none { it.type == RecommendationReasonType.CET_SEASON }) {
                reasons.add(RecommendationReason(RecommendationReasonType.CET_SEASON))
            }
        }

        return reasons
    }

    /**
     * 判断给定日期是否处于四六级考试准备季 / Determine whether the given date falls into the CET exam preparation season
     * 我们将2-5月和8-11月视为四六级考试季 / We treat February-May and August-November as CET seasons
     */
    private fun isCetSeason(date: LocalDate): Boolean {
        val m = date.monthValue
        return (m in 2..5) || (m in 8..11)
    }

    /**
     * 从课程名称构建简单关键词列表 / Build a list of simple keywords from course names
     * 中文和英文名称都用作原始关键词 / Both Chinese and English names are used as raw keywords
     */
    private fun buildKeywordsFromCourses(courses: List<com.example.tradingplatform.data.local.TimetableCourseEntity>): List<String> {
        val keywords = mutableSetOf<String>()
        for (course in courses) {
            val cn = course.courseNameCn.trim()
            val en = course.courseNameEn.trim()
            val code = course.courseCode.trim()

            if (cn.length >= 2) {
                keywords.add(cn)
            }
            if (en.length >= 2) {
                keywords.add(en)
            }
            if (code.isNotEmpty()) {
                keywords.add(code)
            }
        }
        return keywords.toList()
    }

    /**
     * 计算商品的简单基于关键词的匹配分数 / Calculate a very simple keyword-based match score for an item
     * 分数是出现在商品标题/描述/类别中的不同关键词数量，归一化到 [0, 1] / The score is the count of distinct keywords appearing in the item's title/description/category, normalized to [0, 1]
     */
    private fun calculateMatchScore(item: Item, keywords: List<String>): Double {
        if (keywords.isEmpty()) return 0.0

        val text = buildString {
            append(item.title.lowercase())
            append(' ')
            append(item.description.lowercase())
            append(' ')
            append(item.category.lowercase())
        }

        var matches = 0
        val used = mutableSetOf<String>()
        for (rawKeyword in keywords) {
            val keyword = rawKeyword.lowercase().trim()
            if (keyword.length < 2) continue
            if (keyword in used) continue
            if (text.contains(keyword)) {
                used.add(keyword)
                matches++
            }
        }

        if (matches == 0) return 0.0
        // 将分数归一化到 [0, 1] / Normalize score to [0, 1]
        return (matches.toDouble() / keywords.size).coerceIn(0.0, 1.0)
    }
}

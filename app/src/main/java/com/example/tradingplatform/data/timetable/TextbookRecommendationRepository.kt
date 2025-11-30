package com.example.tradingplatform.data.timetable

import android.content.Context
import android.util.Log
import com.example.tradingplatform.data.auth.AuthRepository
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.data.items.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Local textbook recommendation repository based on timetable and posted items.
 *
 * This repository runs fully on device. It uses the student's timetable
 * to find relevant courses and then matches posted items by simple
 * keyword searching on title/description/category.
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
    private val cetKeywords: List<String> = listOf(
        // Chinese keywords
        "四级", "六级", "四六级", "英语四级", "英语六级", "真题", "词汇", "听力",
        // English keywords (stored in lowercase, matched in lowercase)
        "cet-4", "cet4", "cet 4",
        "cet-6", "cet6", "cet 6",
        "college english test", "english exam", "english test",
        "vocabulary", "word list", "listening", "mock test"
    )

    private data class TermWeights(
        val term1Weight: Double,
        val term2Weight: Double
    )

    /**
     * Get recommended items for a given student ID at a specific date.
     * This is intended for local demo and in-app recommendation.
     */
    suspend fun getRecommendedItemsForStudent(
        studentId: String,
        now: LocalDate = LocalDate.now()
    ): List<Item> = withContext(Dispatchers.IO) {
        if (timetableRepository == null || itemRepository == null) {
            Log.w(TAG, "Repositories not initialized, return empty list")
            return@withContext emptyList()
        }

        return@withContext try {
            val effectiveNow = resolveEffectiveDate(now)
            // 1. Query both term-1 and term-2 courses for this student
            val year = effectiveNow.year
            val dateForTerm1 = LocalDate.of(year, 10, 1) // term=1
            val dateForTerm2 = LocalDate.of(year, 3, 1)  // term=2

            val term1Courses = timetableRepository.getCoursesForStudent(studentId, dateForTerm1)
            val term2Courses = timetableRepository.getCoursesForStudent(studentId, dateForTerm2)

            if (term1Courses.isEmpty() && term2Courses.isEmpty()) {
                Log.d(TAG, "No timetable courses found for student: $studentId")
                return@withContext emptyList()
            }

            // 2. Build keyword lists for each term and CET
            val term1Keywords = buildKeywordsFromCourses(term1Courses)
            val term2Keywords = buildKeywordsFromCourses(term2Courses)

            val hasTermKeywords = term1Keywords.isNotEmpty() || term2Keywords.isNotEmpty()
            val cetSeason = isCetSeason(effectiveNow)

            if (!hasTermKeywords && !cetSeason) {
                Log.d(TAG, "No valid keywords built from courses and not CET season")
                return@withContext emptyList()
            }

            // 3. Load items (prefer remote via ItemRepository)
            val allItems = itemRepository.listItems(limit = 200)
            if (allItems.isEmpty()) {
                Log.d(TAG, "No items available for recommendation")
                return@withContext emptyList()
            }

            // 4. Filter out current user's own items
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

            // 5. Score items by simple keyword match with term-based weights
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

                val finalScore = weights.term1Weight * term1Score +
                        weights.term2Weight * term2Score +
                        cetScore

                item to finalScore
            }.filter { it.second > 0.0 }

            if (scored.isEmpty()) {
                Log.d(TAG, "No items matched timetable keywords")
                return@withContext emptyList()
            }

            val sorted = scored.sortedByDescending { it.second }
            val result = sorted.take(MAX_RESULTS).map { it.first }
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
     * Compute weights for first-term (term 1) and second-term (term 2) courses
     * based on the effective calendar month.
     *
     * Rules:
     *  - For second term courses:
     *      - Jan-Feb (1-2): highest weight
     *      - Mar-Jun (3-6): medium weight
     *      - Other months (7-12): lowest weight
     *  - For first term courses:
     *      - Jul-Aug (7-8): highest weight
     *      - Sep-Dec (9-12): medium weight
     *      - Other months (1-6): lowest weight
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

    /**
     * Determine whether the given date falls into the CET exam preparation season.
     * We treat February-May and August-November as CET seasons.
     */
    private fun isCetSeason(date: LocalDate): Boolean {
        val m = date.monthValue
        return (m in 2..5) || (m in 8..11)
    }

    /**
     * Build a list of simple keywords from course names.
     * Both Chinese and English names are used as raw keywords.
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
     * Calculate a very simple keyword-based match score for an item.
     * The score is the count of distinct keywords appearing in
     * the item's title/description/category, normalized to [0, 1].
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
        // Normalize score to [0, 1]
        return (matches.toDouble() / keywords.size).coerceIn(0.0, 1.0)
    }
}

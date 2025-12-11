package com.example.tradingplatform.data.timetable

import android.content.Context
import android.util.Log
import com.example.tradingplatform.data.local.AppDatabase
import com.example.tradingplatform.data.local.TimetableCourseDao
import com.example.tradingplatform.data.local.TimetableCourseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * 课表仓库：根据学号和当前日期查询本学期课程 / Timetable repository: query current semester courses by student ID and current date
 * 当前为 DEMO 版本：/ Current DEMO version:
 *  - 学年固定使用 2025-2026 的数据 / Academic year fixed to 2025-2026 data
 *  - 学年起始年固定为 2025，用于计算年级（1-4）/ Academic year start fixed to 2025, used to calculate grade (1-4)
 *  - 学期根据当前月份粗略判断：2-7 月视为下学期(term=2)，其它视为上学期(term=1) / Term roughly determined by current month: Feb-Jul as term 2, others as term 1
 */
class TimetableRepository(
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "TimetableRepository"
        private const val DEMO_ACADEMIC_YEAR_START = 2025
    }

    private val database: AppDatabase? = context?.let { AppDatabase.getDatabase(it) }
    private val timetableDao: TimetableCourseDao? = database?.timetableCourseDao()

    /**
     * 根据学号和当前日期查询本学期课程列表 / Query current semester course list by student ID and current date
     */
    suspend fun getCoursesForStudent(
        studentId: String,
        now: LocalDate = LocalDate.now()
    ): List<TimetableCourseEntity> = withContext(Dispatchers.IO) {
        if (timetableDao == null) {
            Log.w(TAG, "timetableDao 为 null，返回空列表")
            return@withContext emptyList()
        }

        return@withContext try {
            val profile = StudentIdParser.parse(studentId)
            val term = computeTerm(now)
            val gradeLevel = computeGradeLevel(profile.entryYear)

            Log.d(
                TAG,
                "查询课程: studentId=$studentId, entryYear=${profile.entryYear}, major=${profile.major}, term=$term, grade=$gradeLevel"
            )

            timetableDao.getCourses(
                major = profile.major,
                gradeLevel = gradeLevel,
                term = term
            )
        } catch (e: Exception) {
            Log.e(TAG, "查询课表失败", e)
            emptyList()
        }
    }

    /**
     * DEMO：根据当前月份推断学期 / DEMO: Infer term from current month
     *  - 2-7 月：视为下学期 term=2 / Feb-Jul: considered as term 2
     *  - 其它月份：视为上学期 term=1 / Other months: considered as term 1
     */
    private fun computeTerm(now: LocalDate): Int {
        val month = now.monthValue
        return if (month in 2..7) 2 else 1
    }

    /**
     * DEMO：根据入学年份计算当前年级（1-4），使用固定学年起始年 2025 / DEMO: Calculate current grade (1-4) from entry year, using fixed academic year start 2025
     */
    private fun computeGradeLevel(entryYear: Int): Int {
        val level = DEMO_ACADEMIC_YEAR_START - entryYear + 1
        return level.coerceIn(1, 4)
    }
}

package com.example.tradingplatform.data.timetable

import android.content.Context
import android.util.Log
import com.example.tradingplatform.data.local.AppDatabase
import com.example.tradingplatform.data.local.TimetableCourseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * 课表初始化工具：从 assets 中的 JSON 导入课程数据到 Room / Timetable initialization utility: import course data from JSON in assets to Room
 */
object TimetableInitializer {

    private const val TAG = "TimetableInitializer"
    private const val ASSET_FILE_NAME = "timetable_courses_2025_2026.json"

    /**
     * 如果课表表为空，则从 assets/timetable_courses_2025_2026.json 导入数据 / If timetable table is empty, import data from assets/timetable_courses_2025_2026.json
     */
    suspend fun ensureInitialized(context: Context) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val dao = db.timetableCourseDao()

            val existingCount = dao.getCourseCount()
            if (existingCount > 0) {
                Log.d(TAG, "Timetable already initialized with $existingCount courses")
                return@withContext
            }

            try {
                val assetManager = context.assets
                assetManager.open(ASSET_FILE_NAME).use { inputStream ->
                    val jsonText = inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(jsonText)
                    val courses = mutableListOf<TimetableCourseEntity>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val academicYear = obj.getString("academic_year")
                        val term = obj.getInt("term")
                        val major = obj.getString("major")
                        val gradeLevel = obj.getInt("grade_level")
                        val courseCode = obj.getString("course_code")
                        val courseNameCn = obj.optString("course_name_cn", "")
                        val courseNameEn = obj.optString("course_name_en", "")

                        courses.add(
                            TimetableCourseEntity(
                                academicYear = academicYear,
                                term = term,
                                major = major,
                                gradeLevel = gradeLevel,
                                courseCode = courseCode,
                                courseNameCn = courseNameCn,
                                courseNameEn = courseNameEn
                            )
                        )
                    }

                    if (courses.isNotEmpty()) {
                        dao.insertCourses(courses)
                        Log.d(TAG, "Inserted ${courses.size} timetable courses from asset")
                    } else {
                        Log.w(TAG, "No courses found in JSON asset")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize timetable from assets", e)
            }
        }
    }
}

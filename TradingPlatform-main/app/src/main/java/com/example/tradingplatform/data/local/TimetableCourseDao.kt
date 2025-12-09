package com.example.tradingplatform.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * 课表课程数据访问对象
 */
@Dao
interface TimetableCourseDao {
    @Query(
        "SELECT * FROM timetable_courses " +
            "WHERE major = :major AND grade_level = :gradeLevel AND term = :term"
    )
    suspend fun getCourses(
        major: String,
        gradeLevel: Int,
        term: Int
    ): List<TimetableCourseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<TimetableCourseEntity>)

    @Query("SELECT COUNT(*) FROM timetable_courses")
    suspend fun getCourseCount(): Int
}

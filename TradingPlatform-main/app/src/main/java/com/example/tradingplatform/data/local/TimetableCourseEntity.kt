package com.example.tradingplatform.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * 课表课程实体（只存课程级别信息，不含节次/教室/老师）
 */
@Entity(
    tableName = "timetable_courses",
    primaryKeys = ["academic_year", "term", "major", "grade_level", "course_code"]
)
data class TimetableCourseEntity(
    @ColumnInfo(name = "academic_year")
    val academicYear: String,
    val term: Int,
    val major: String,
    @ColumnInfo(name = "grade_level")
    val gradeLevel: Int,
    @ColumnInfo(name = "course_code")
    val courseCode: String,
    @ColumnInfo(name = "course_name_cn")
    val courseNameCn: String,
    @ColumnInfo(name = "course_name_en")
    val courseNameEn: String
)

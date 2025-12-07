package com.example.tradingplatform.data.timetable

/**
 * 学号解析工具：根据 8 位学号解析入学年份和专业
 * 规则示例：24373302
 *  - 前两位：24 -> 入学年份 2024
 *  - 第 5 位：1/2/3/4 -> IOT/SE/FIN/EIE
 */

data class StudentProfile(
    val entryYear: Int,
    val major: String
)

object StudentIdParser {

    /**
     * 解析 BDIC 学号，返回入学年份和专业代码
     */
    fun parse(studentId: String): StudentProfile {
        require(studentId.length >= 6) { "学号格式不正确: $studentId" }

        // 前两位：入学年份后两位，例如 23 -> 2023
        val yearPart = studentId.substring(0, 2)
        val entryYear = 2000 + yearPart.toInt()

        // 第 5 位：1/2/3/4 -> IOT/SE/FIN/EIE
        val majorDigit = studentId[4]
        val major = when (majorDigit) {
            '1' -> "IOT" // 物联网工程
            '2' -> "SE"  // 软件工程
            '3' -> "FIN" // 金融学
            '4' -> "EIE" // 电子信息工程
            else -> throw IllegalArgumentException("未知专业代码: $majorDigit in $studentId")
        }

        return StudentProfile(entryYear = entryYear, major = major)
    }
}

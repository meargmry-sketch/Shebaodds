package com.example.util

import java.util.Calendar

data class EthiopianDate(
    val year: Int,
    val month: Int, // 1 to 13
    val day: Int,
    val monthNameEn: String,
    val monthNameAm: String,
    val monthNameTi: String,
    val monthNameOm: String
)

object EthiopianDateHelper {

    private val monthNamesEn = listOf(
        "Mäskäräm", "Teqemt", "Hedar", "Tahsas", "Tir", "Yakatit", 
        "Megabit", "Miyazya", "Genbot", "Sene", "Hamle", "Nehase", "Pagume"
    )

    private val monthNamesAm = listOf(
        "መስከረም", "ጥቅምት", "ኅዳር", "ታኅሣሥ", "ጥር", "የካቲት", 
        "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰኔ", "ሐምሌ", "ነሐሴ", "ጳጉሜ"
    )

    private val monthNamesTi = listOf(
        "መስከረም", "ጥቅምቲ", "ሕዳር", "ታሕሳስ", "ጥሪ", "ለካቲት", 
        "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰነ", "ሓምለ", "ነሓሰ", "ጳጉሜን"
    )

    private val monthNamesOm = listOf(
        "Fulbaana", "Onkololeessa", "Sadaasa", "Muddee", "Amajjii", "Guraandhala", 
        "Bitootessa", "Eebila", "Caamsaa", "Waxabajjii", "Adoolessa", "Hagayya", "Qaammee"
    )

    fun convertGregorianToEthiopian(gregorianYear: Int, gregorianMonth: Int, gregorianDay: Int): EthiopianDate {
        var ethiopianYear = gregorianYear - 8
        val isEthiopianLeap = (ethiopianYear % 4 == 3)
        val startDayInGregorian = if (isEthiopianLeap) 12 else 11
        
        val cal = Calendar.getInstance()
        cal.set(gregorianYear, gregorianMonth - 1, gregorianDay)
        val timeInMillis = cal.timeInMillis
        
        val startCal = Calendar.getInstance()
        var startYear = gregorianYear
        if (gregorianMonth < 9 || (gregorianMonth == 9 && gregorianDay < startDayInGregorian)) {
            startYear -= 1
        } else {
            ethiopianYear = gregorianYear - 7
        }
        
        val startIsLeap = ((startYear - 8) % 4 == 3)
        val startDay = if (startIsLeap) 12 else 11
        startCal.set(startYear, 8, startDay, 12, 0, 0)
        
        val diffDays = ((timeInMillis - startCal.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
        
        var ethMonth = (diffDays / 30) + 1
        var ethDay = (diffDays % 30) + 1
        
        if (ethMonth > 13) {
            ethMonth = 13
            ethDay = diffDays - 360 + 1
        }
        
        val monthIdx = (ethMonth - 1).coerceIn(0, 12)
        
        return EthiopianDate(
            year = ethiopianYear,
            month = ethMonth,
            day = ethDay,
            monthNameEn = monthNamesEn[monthIdx],
            monthNameAm = monthNamesAm[monthIdx],
            monthNameTi = monthNamesTi[monthIdx],
            monthNameOm = monthNamesOm[monthIdx]
        )
    }

    fun getEthiopianTime(hour24: Int, minute: Int): String {
        val ethHour = (hour24 - 6 + 12) % 12
        val ethHourDisplay = if (ethHour == 0) 12 else ethHour
        val isDay = hour24 in 6..17
        
        val amhPart = if (isDay) "ቀን" else "ማታ"
        return String.format("%02d:%02d (%s)", ethHourDisplay, minute, amhPart)
    }

    fun getEthiopianTimeWithLang(hour24: Int, minute: Int, lang: String): String {
        val ethHour = (hour24 - 6 + 12) % 12
        val ethHourDisplay = if (ethHour == 0) 12 else ethHour
        val isDay = hour24 in 6..17
        
        val period = when (lang) {
            "am" -> if (isDay) "ቀን" else "ማታ"
            "ti" -> if (isDay) "መዓልቲ" else "ምሸት"
            "om" -> if (isDay) "Guyyaa" else "Halkan"
            else -> if (isDay) "Day" else "Night"
        }
        return String.format("%02d:%02d %s", ethHourDisplay, minute, period)
    }
}

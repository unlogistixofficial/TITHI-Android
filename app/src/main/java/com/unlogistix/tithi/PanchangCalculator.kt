
package com.unlogistix.tithi

import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.searchRiseSet
import io.github.cosinekitty.astronomy.sunPosition

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.SimpleTimeZone
import java.util.TimeZone
import kotlin.math.floor

data class PanchangData(
    val date: String,
    val tithi: String,
    val paksha: String,
    val nakshatra: String,
    val yoga: String,
    val karana: String,
    val sunrise: String,
    val sunset: String,
    val calculationNote: String
)

object PanchangCalculator {

    private val dhakaTimeZone: TimeZone =
        TimeZone.getTimeZone("Asia/Dhaka")

    private val utcTimeZone: TimeZone =
        TimeZone.getTimeZone("UTC")

    private val timeFormatter = java.text.SimpleDateFormat(
        "hh:mm a",
        Locale.ENGLISH
    ).apply {
        timeZone = dhakaTimeZone
    }

    private val tithiNames = listOf(
        "প্রতিপদ",
        "দ্বিতীয়া",
        "তৃতীয়া",
        "চতুর্থী",
        "পঞ্চমী",
        "ষষ্ঠী",
        "সপ্তমী",
        "অষ্টমী",
        "নবমী",
        "দশমী",
        "একাদশী",
        "দ্বাদশী",
        "ত্রয়োদশী",
        "চতুর্দশী",
        "পূর্ণিমা",
        "প্রতিপদ",
        "দ্বিতীয়া",
        "তৃতীয়া",
        "চতুর্থী",
        "পঞ্চমী",
        "ষষ্ঠী",
        "সপ্তমী",
        "অষ্টমী",
        "নবমী",
        "দশমী",
        "একাদশী",
        "দ্বাদশী",
        "ত্রয়োদশী",
        "চতুর্দশী",
        "অমাবস্যা"
    )

    private val nakshatraNames = listOf(
        "অশ্বিনী",
        "ভরণী",
        "কৃত্তিকা",
        "রোহিণী",
        "মৃগশিরা",
        "আর্দ্রা",
        "পুনর্বসু",
        "পুষ্যা",
        "অশ্লেষা",
        "মঘা",
        "পূর্বফাল্গুনী",
        "উত্তরফাল্গুনী",
        "হস্তা",
        "চিত্রা",
        "স্বাতী",
        "বিশাখা",
        "অনুরাধা",
        "জ্যেষ্ঠা",
        "মূলা",
        "পূর্বাষাঢ়া",
        "উত্তরাষাঢ়া",
        "শ্রবণা",
        "ধনিষ্ঠা",
        "শতভিষা",
        "পূর্বভাদ্রপদ",
        "উত্তরভাদ্রপদ",
        "রেবতী"
    )

    private val yogaNames = listOf(
        "বিষ্কম্ভ",
        "প্রীতি",
        "আয়ুষ্মান",
        "সৌভাগ্য",
        "শোভন",
        "অতিগণ্ড",
        "সুকর্মা",
        "ধৃতি",
        "শূল",
        "গণ্ড",
        "বৃদ্ধি",
        "ধ্রুব",
        "ব্যাঘাত",
        "হর্ষণ",
        "বজ্র",
        "সিদ্ধি",
        "ব্যতীপাত",
        "বরীয়ান",
        "পরিঘ",
        "শিব",
        "সিদ্ধ",
        "সাধ্য",
        "শুভ",
        "শুক্ল",
        "ব্রহ্ম",
        "ইন্দ্র",
        "বৈধৃতি"
    )

    private val movableKaranas = listOf(
        "বব",
        "বালব",
        "কৌলব",
        "তৈতিল",
        "গর",
        "বণিজ",
        "বিষ্টি"
    )

    private val fixedKaranas = listOf(
        "কিমস্তুঘ্ন",
        "শকুনি",
        "চতুষ্পদ",
        "নাগ"
    )

    private fun normalizeAngle(angle: Double): Double {
        return ((angle % 360.0) + 360.0) % 360.0
    }

    /*
     * Converts local Bangladesh midnight into Astronomy Engine UTC time.
     */
    private fun createLocalMidnight(
        year: Int,
        month: Int,
        day: Int
    ): Time {

        val localCalendar = GregorianCalendar(
            dhakaTimeZone,
            Locale.US
        ).apply {
            isLenient = false
            clear()
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val utcCalendar = Calendar.getInstance(utcTimeZone).apply {
            timeInMillis = localCalendar.timeInMillis
        }

        return Time(
            utcCalendar.get(Calendar.YEAR),
            utcCalendar.get(Calendar.MONTH) + 1,
            utcCalendar.get(Calendar.DAY_OF_MONTH),
            utcCalendar.get(Calendar.HOUR_OF_DAY),
            utcCalendar.get(Calendar.MINUTE),
            utcCalendar.get(Calendar.SECOND).toDouble()
        )
    }

    private fun formatAstronomyTime(time: Time?): String {
        if (time == null) {
            return "তথ্য পাওয়া যায়নি"
        }

        val date = Date(time.toMillisecondsSince1970())

        return synchronized(timeFormatter) {
            timeFormatter.format(date)
        }
    }

    /*
     * Preliminary sidereal adjustment.
     * This is NOT a verified Vishuddha Siddhanta ayanamsa.
     */
    private fun approximateAyanamsa(
        year: Int,
        month: Int,
        day: Int
    ): Double {

        val calendar = GregorianCalendar(
            TimeZone.getTimeZone("UTC"),
            Locale.US
        ).apply {
            clear()
            set(year, month - 1, day)
        }

        val decimalYear = year +
            (calendar.get(Calendar.DAY_OF_YEAR) - 1) / 365.2425

        val yearsFrom2000 = decimalYear - 2000.0

        val precessionPerYear = 50.290966 / 3600.0

        return 23.85675 + yearsFrom2000 * precessionPerYear
    }

    private fun getKarana(index: Int): String {

        return when (index) {
            0 -> fixedKaranas[0]

            in 1..56 -> {
                movableKaranas[(index - 1) % 7]
            }

            57 -> fixedKaranas[1]
            58 -> fixedKaranas[2]
            59 -> fixedKaranas[3]

            else -> "তথ্য পাওয়া যায়নি"
        }
    }

    fun calculate(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double = 23.8103,
        longitude: Double = 90.4125
    ): PanchangData {

        require(month in 1..12) {
            "Month must be between 1 and 12"
        }

        require(latitude in -90.0..90.0) {
            "Invalid latitude"
        }

        require(longitude in -180.0..180.0) {
            "Invalid longitude"
        }

        val localMidnight = createLocalMidnight(
            year,
            month,
            day
        )

        val observer = Observer(
            latitude,
            longitude,
            0.0
        )

        // Find sunrise and sunset for the selected local date.
        val sunrise = searchRiseSet(
            Body.Sun,
            observer,
            Direction.Rise,
            localMidnight,
            1.0
        )

        val sunset = searchRiseSet(
            Body.Sun,
            observer,
            Direction.Set,
            localMidnight,
            1.0
        )

        // Panchang values are evaluated at sunrise.
        // Local noon is used only if sunrise cannot be found.
        val calculationTime = sunrise ?: localMidnight.addDays(0.5)

        val sunLongitude = normalizeAngle(
            sunPosition(calculationTime).elon
        )

        val moonLongitude = normalizeAngle(
            eclipticGeoMoon(calculationTime).lon
        )

        // Tithi is based on the angular distance between Moon and Sun.
        val elongation = normalizeAngle(
            moonLongitude - sunLongitude
        )

        val tithiIndex = floor(
            elongation / 12.0
        ).toInt().coerceIn(0, 29)

        val paksha = if (tithiIndex < 15) {
            "শুক্লপক্ষ"
        } else {
            "কৃষ্ণপক্ষ"
        }

        // Preliminary sidereal longitude calculation.
        val ayanamsa = approximateAyanamsa(
            year,
            month,
            day
        )

        val siderealMoon = normalizeAngle(
            moonLongitude - ayanamsa
        )

        val siderealSun = normalizeAngle(
            sunLongitude - ayanamsa
        )

        // Nakshatra: 360 / 27 degrees per division.
        val nakshatraIndex = floor(
            siderealMoon / (360.0 / 27.0)
        ).toInt().coerceIn(0, 26)

        // Yoga: sum of sidereal Sun and Moon longitudes.
        val yogaLongitude = normalizeAngle(
            siderealSun + siderealMoon
        )

        val yogaIndex = floor(
            yogaLongitude / (360.0 / 27.0)
        ).toInt().coerceIn(0, 26)

        // Each Karana covers 6 degrees of lunar elongation.
        val karanaIndex = floor(
            elongation / 6.0
        ).toInt().coerceIn(0, 59)

        val dateLabel = String.format(
            Locale.US,
            "%04d-%02d-%02d",
            year,
            month,
            day
        )

        return PanchangData(
            date = dateLabel,
            tithi = tithiNames[tithiIndex],
            paksha = paksha,
            nakshatra = nakshatraNames[nakshatraIndex],
            yoga = yogaNames[yogaIndex],
            karana = getKarana(karanaIndex),
            sunrise = formatAstronomyTime(sunrise),
            sunset = formatAstronomyTime(sunset),
            calculationNote =
                "প্রাথমিক জ্যোতির্বৈজ্ঞানিক হিসাব; বিশুদ্ধ সিদ্ধান্ত পঞ্জিকার সঙ্গে যাচাই বাকি"
        )
    }
}

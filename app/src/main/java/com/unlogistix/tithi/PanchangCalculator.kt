
package com.unlogistix.tithi

import io.github.cosinekitty.astronomy.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    private val zone = ZoneId.of("Asia/Dhaka")
    private val timeFormat = DateTimeFormatter.ofPattern(
        "hh:mm a", Locale.ENGLISH
    )

    private val tithiNames = listOf(
        "প্রতিপদ", "দ্বিতীয়া", "তৃতীয়া", "চতুর্থী",
        "পঞ্চমী", "ষষ্ঠী", "সপ্তমী", "অষ্টমী",
        "নবমী", "দশমী", "একাদশী", "দ্বাদশী",
        "ত্রয়োদশী", "চতুর্দশী", "পূর্ণিমা",
        "প্রতিপদ", "দ্বিতীয়া", "তৃতীয়া", "চতুর্থী",
        "পঞ্চমী", "ষষ্ঠী", "সপ্তমী", "অষ্টমী",
        "নবমী", "দশমী", "একাদশী", "দ্বাদশী",
        "ত্রয়োদশী", "চতুর্দশী", "অমাবস্যা"
    )

    private val nakshatraNames = listOf(
        "অশ্বিনী", "ভরণী", "কৃত্তিকা", "রোহিণী",
        "মৃগশিরা", "আর্দ্রা", "পুনর্বসু", "পুষ্যা",
        "অশ্লেষা", "মঘা", "পূর্বফাল্গুনী", "উত্তরফাল্গুনী",
        "হস্তা", "চিত্রা", "স্বাতী", "বিশাখা",
        "অনুরাধা", "জ্যেষ্ঠা", "মূলা", "পূর্বাষাঢ়া",
        "উত্তরাষাঢ়া", "শ্রবণা", "ধনিষ্ঠা", "শতভিষা",
        "পূর্বভাদ্রপদ", "উত্তরভাদ্রপদ", "রেবতী"
    )

    private val yogaNames = listOf(
        "বিষ্কম্ভ", "প্রীতি", "আয়ুষ্মান", "সৌভাগ্য",
        "শোভন", "অতিগণ্ড", "সুকর্মা", "ধৃতি",
        "শূল", "গণ্ড", "বৃদ্ধি", "ধ্রুব",
        "ব্যাঘাত", "হর্ষণ", "বজ্র", "সিদ্ধি",
        "ব্যতীপাত", "বরীয়ান", "পরিঘ", "শিব",
        "সিদ্ধ", "সাধ্য", "শুভ", "শুক্ল",
        "ব্রহ্ম", "ইন্দ্র", "বৈধৃতি"
    )

    private val movableKaranas = listOf(
        "বব", "বালব", "কৌলব", "তৈতিল",
        "গর", "বণিজ", "বিষ্টি"
    )

    private val fixedKaranas = listOf(
        "কিমস্তুঘ্ন", "শকুনি", "চতুষ্পদ", "নাগ"
    )

    private fun normalize(angle: Double): Double {
        return ((angle % 360.0) + 360.0) % 360.0
    }

    private fun toAstronomyTime(
        date: LocalDate,
        hour: Int = 0,
        minute: Int = 0
    ): Time {
        val instant = date.atTime(hour, minute)
            .atZone(zone)
            .withZoneSameInstant(ZoneOffset.UTC)

        return Time(
            instant.year,
            instant.monthValue,
            instant.dayOfMonth,
            instant.hour,
            instant.minute,
            instant.second.toDouble()
        )
    }

    private fun formatTime(time: Time?): String {
        if (time == null) return "তথ্য পাওয়া যায়নি"

        return time.toMillisecondsSince1970()
            .let { java.time.Instant.ofEpochMilli(it) }
            .atZone(zone)
            .format(timeFormat)
    }

    private fun getAyanamsa(date: LocalDate): Double {
        val years = date.year - 2000.0
        return 23.85675 + (years * 0.013968)
    }

    private fun getKarana(index: Int): String {
        return when (index) {
            0 -> fixedKaranas[0]
            in 1..56 -> movableKaranas[(index - 1) % 7]
            57 -> fixedKaranas[1]
            58 -> fixedKaranas[2]
            else -> fixedKaranas[3]
        }
    }

    fun calculate(
        date: LocalDate,
        latitude: Double = 23.8103,
        longitude: Double = 90.4125
    ): PanchangData {

        val observer = Observer(latitude, longitude, 0.0)

        // Panchang is calculated at local sunrise.
        val midnight = toAstronomyTime(date)

        val sunrise = searchRiseSet(
            Body.SUN,
            observer,
            Direction.RISE,
            midnight,
            1.0
        )

        val sunset = searchRiseSet(
            Body.SUN,
            observer,
            Direction.SET,
            midnight,
            1.0
        )

        val calculationTime = sunrise ?: toAstronomyTime(date, 12, 0)

        val sunLongitude = normalize(
            sunPosition(calculationTime).elon
        )

        val moonLongitude = normalize(
            eclipticGeoMoon(calculationTime).lon
        )

        // Tithi: angular difference between Moon and Sun.
        val elongation = normalize(moonLongitude - sunLongitude)
        val tithiIndex = (elongation / 12.0)
            .toInt()
            .coerceIn(0, 29)

        val paksha = if (tithiIndex < 15) {
            "শুক্লপক্ষ"
        } else {
            "কৃষ্ণপক্ষ"
        }

        // Sidereal positions for Nakshatra and Yoga.
        val ayanamsa = getAyanamsa(date)

        val siderealMoon = normalize(moonLongitude - ayanamsa)
        val siderealSun = normalize(sunLongitude - ayanamsa)

        val nakshatraIndex = (
            siderealMoon / (360.0 / 27.0)
        ).toInt().coerceIn(0, 26)

        val yogaIndex = (
            normalize(siderealMoon + siderealSun) / (360.0 / 27.0)
        ).toInt().coerceIn(0, 26)

        val karanaIndex = (elongation / 6.0)
            .toInt()
            .coerceIn(0, 59)

        return PanchangData(
            date = date.toString(),
            tithi = tithiNames[tithiIndex],
            paksha = paksha,
            nakshatra = nakshatraNames[nakshatraIndex],
            yoga = yogaNames[yogaIndex],
            karana = getKarana(karanaIndex),
            sunrise = formatTime(sunrise),
            sunset = formatTime(sunset),
            calculationNote = "প্রাথমিক জ্যোতির্বৈজ্ঞানিক হিসাব; পঞ্জিকা-যাচাই বাকি"
        )
    }
}

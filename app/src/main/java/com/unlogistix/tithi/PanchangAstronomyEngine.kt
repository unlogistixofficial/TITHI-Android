package com.unlogistix.tithi

import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.searchRiseSet
import io.github.cosinekitty.astronomy.sunPosition
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.floor

/**
 * Preliminary astronomical foundation for TITHI.
 *
 * This is not yet a complete Vishuddha Siddhanta engine.
 */
object PanchangAstronomyEngine {

    private const val TIME_ZONE_ID = "Asia/Dhaka"
    private const val J2000_UNIX_MILLIS = 946728000000L
    private const val MILLIS_PER_DAY = 86400000.0
    private const val TITHI_ANGLE = 12.0
    private const val SEARCH_STEP_MILLIS = 1800000L
    private const val MAX_SEARCH_STEPS = 96

    private val tithiNamesBn = listOf(
        "প্রতিপদ", "দ্বিতীয়া", "তৃতীয়া",
        "চতুর্থী", "পঞ্চমী", "ষষ্ঠী",
        "সপ্তমী", "অষ্টমী", "নবমী",
        "দশমী", "একাদশী", "দ্বাদশী",
        "ত্রয়োদশী", "চতুর্দশী", "পূর্ণিমা / অমাবস্যা"
    )

    private val tithiNamesEn = listOf(
        "Pratipada", "Dwitiya", "Tritiya",
        "Chaturthi", "Panchami", "Shashthi",
        "Saptami", "Ashtami", "Navami",
        "Dashami", "Ekadashi", "Dwadashi",
        "Trayodashi", "Chaturdashi", "Purnima / Amavasya"
    )

    fun calculate(
        dateMillis: Long,
        location: PanchangLocation
    ): PanchangCalculationResult {

        require(location.timeZoneId == TIME_ZONE_ID) {
            "Only Asia/Dhaka is supported currently"
        }

        val localMidnight = getLocalMidnight(dateMillis)

        val observer = Observer(
            location.latitude,
            location.longitude,
            0.0
        )

        val midnightTime = toAstronomyTime(localMidnight)

        val sunrise = searchRiseSet(
            Body.Sun,
            observer,
            Direction.Rise,
            midnightTime,
            1.0
        )

        val sunset = sunrise?.let { riseTime ->
            searchRiseSet(
                Body.Sun,
                observer,
                Direction.Set,
                toAstronomyTime(
                    riseTime.toMillisecondsSince1970() + 1000L
                ),
                1.0
            )
        }

        if (sunrise == null) {
            return PanchangCalculationResult(
                dateMillis = dateMillis,
                location = location,
                calculationMethod =
                    "Astronomy Engine — sunrise unavailable",
                isVerified = false
            )
        }

        val sunriseMillis =
            sunrise.toMillisecondsSince1970()

        val elongation =
            getMoonSunElongation(sunriseMillis)

        val tithiNumber =
            (floor(elongation / TITHI_ANGLE).toInt() % 30) + 1

        val paksha = if (tithiNumber <= 15) {
            PanchangPaksha.SHUKLA
        } else {
            PanchangPaksha.KRISHNA
        }

        val pakshaTithiNumber =
            if (tithiNumber <= 15) {
                tithiNumber
            } else {
                tithiNumber - 15
            }

        val nameIndex = pakshaTithiNumber - 1

        val tithi = PanchangElement(
            number = pakshaTithiNumber,
            nameBn = tithiNamesBn[nameIndex],
            nameEn = tithiNamesEn[nameIndex],
            endsAtMillis = findNextTithiEnd(sunriseMillis)
        )

        return PanchangCalculationResult(
            dateMillis = dateMillis,
            location = location,
            sunriseMillis = sunriseMillis,
            sunsetMillis = sunset?.toMillisecondsSince1970(),
            tithi = tithi,
            paksha = paksha,
            nakshatra = null,
            yoga = null,
            karana = null,
            calculationMethod =
                "Astronomy Engine — preliminary tropical longitude method",
            isVerified = false
        )
    }

    private fun getMoonSunElongation(
        utcMillis: Long
    ): Double {

        val time = toAstronomyTime(utcMillis)

        val sunLongitude = sunPosition(time).elon
        val moonLongitude = eclipticGeoMoon(time).lon

        return normalizeDegrees(moonLongitude - sunLongitude)
    }

    private fun findNextTithiEnd(
        startMillis: Long
    ): Long? {

        val initialAngle =
            getMoonSunElongation(startMillis)

        val currentSector =
            floor(initialAngle / TITHI_ANGLE).toInt()

        val targetProgress =
            ((currentSector + 1) * TITHI_ANGLE) - initialAngle

        var previousMillis = startMillis

        for (step in 1..MAX_SEARCH_STEPS) {

            val currentMillis =
                startMillis + step * SEARCH_STEP_MILLIS

            val currentAngle =
                getMoonSunElongation(currentMillis)

            val progress =
                normalizeDegrees(currentAngle - initialAngle)

            if (progress >= targetProgress) {

                var low = previousMillis
                var high = currentMillis

                while (high - low > 1000L) {

                    val middle = low + (high - low) / 2L

                    val middleAngle =
                        getMoonSunElongation(middle)

                    val middleProgress =
                        normalizeDegrees(middleAngle - initialAngle)

                    if (middleProgress >= targetProgress) {
                        high = middle
                    } else {
                        low = middle
                    }
                }

                return high
            }

            previousMillis = currentMillis
        }

        return null
    }

    private fun getLocalMidnight(
        dateMillis: Long
    ): Long {

        val calendar = Calendar.getInstance(
            TimeZone.getTimeZone(TIME_ZONE_ID)
        )

        calendar.timeInMillis = dateMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    private fun toAstronomyTime(
        utcMillis: Long
    ): Time {

        val universalDays =
            (utcMillis - J2000_UNIX_MILLIS) / MILLIS_PER_DAY

        return Time(universalDays)
    }

    private fun normalizeDegrees(
        degrees: Double
    ): Double {

        return ((degrees % 360.0) + 360.0) % 360.0
    }
}

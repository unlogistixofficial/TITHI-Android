
package com.unlogistix.tithi

enum class PanchangPaksha {
    SHUKLA,
    KRISHNA
}

data class PanchangElement(
    val number: Int,
    val nameBn: String,
    val nameEn: String,
    val endsAtMillis: Long? = null
)

data class PanchangCalculationResult(
    val dateMillis: Long,
    val location: PanchangLocation,
    val sunriseMillis: Long? = null,
    val sunsetMillis: Long? = null,
    val tithi: PanchangElement? = null,
    val paksha: PanchangPaksha? = null,
    val nakshatra: PanchangElement? = null,
    val yoga: PanchangElement? = null,
    val karana: PanchangElement? = null,
    val calculationMethod: String,
    val isVerified: Boolean = false
)

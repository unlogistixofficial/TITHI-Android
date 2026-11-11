
package com.unlogistix.tithi

/**
 * User-selected location for Panchang calculations.
 * Coordinates are stored in decimal degrees.
 */
data class PanchangLocation(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String = "Asia/Dhaka"
) {
    init {
        require(id.isNotBlank()) { "Location ID cannot be empty" }
        require(nameBn.isNotBlank()) { "Bengali name cannot be empty" }
        require(nameEn.isNotBlank()) { "English name cannot be empty" }
        require(latitude in -90.0..90.0) { "Invalid latitude" }
        require(longitude in -180.0..180.0) { "Invalid longitude" }
        require(timeZoneId == "Asia/Dhaka") {
            "TITHI currently supports Bangladesh time only"
        }
    }
}


package com.unlogistix.tithi

/**
 * Initial city catalogue for Bangladesh.
 * Coordinates are approximate city-centre coordinates.
 * They will later be refined with user-selected GPS coordinates.
 */
object PanchangLocationCatalog {

    val cities: List<PanchangLocation> = listOf(

        PanchangLocation(
            id = "dhaka",
            nameBn = "ঢাকা",
            nameEn = "Dhaka",
            latitude = 23.8103,
            longitude = 90.4125
        ),

        PanchangLocation(
            id = "sylhet",
            nameBn = "সিলেট",
            nameEn = "Sylhet",
            latitude = 24.8949,
            longitude = 91.8687
        ),

        PanchangLocation(
            id = "chattogram",
            nameBn = "চট্টগ্রাম",
            nameEn = "Chattogram",
            latitude = 22.3569,
            longitude = 91.7832
        ),

        PanchangLocation(
            id = "rajshahi",
            nameBn = "রাজশাহী",
            nameEn = "Rajshahi",
            latitude = 24.3745,
            longitude = 88.6042
        ),

        PanchangLocation(
            id = "khulna",
            nameBn = "খুলনা",
            nameEn = "Khulna",
            latitude = 22.8456,
            longitude = 89.5403
        ),

        PanchangLocation(
            id = "barishal",
            nameBn = "বরিশাল",
            nameEn = "Barishal",
            latitude = 22.7010,
            longitude = 90.3535
        ),

        PanchangLocation(
            id = "rangpur",
            nameBn = "রংপুর",
            nameEn = "Rangpur",
            latitude = 25.7439,
            longitude = 89.2752
        ),

        PanchangLocation(
            id = "mymensingh",
            nameBn = "ময়মনসিংহ",
            nameEn = "Mymensingh",
            latitude = 24.7471,
            longitude = 90.4203
        )
    )

    fun findById(id: String): PanchangLocation? {
        return cities.find { it.id == id }
    }
}

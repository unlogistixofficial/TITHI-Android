
package com.unlogistix.tithi

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val PanchangCream = Color(0xFFF8F6F0)
private val PanchangWhite = Color(0xFFFFFFFF)
private val PanchangNavy = Color(0xFF244B70)
private val PanchangText = Color(0xFF282D32)
private val PanchangMuted = Color(0xFF777D83)
private val PanchangSoftBlue = Color(0xFFEAF0F6)
private val PanchangBorder = Color(0xFFE9E5DC)
private val PanchangGold = Color(0xFF9B7842)

private data class PanchangCity(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

private val panchangCities = listOf(
    PanchangCity("ঢাকা", 23.8103, 90.4125),
    PanchangCity("সিলেট", 24.8949, 91.8687),
    PanchangCity("চট্টগ্রাম", 22.3569, 91.7832),
    PanchangCity("রাজশাহী", 24.3745, 88.6042),
    PanchangCity("খুলনা", 22.8456, 89.5403),
    PanchangCity("বরিশাল", 22.7010, 90.3535),
    PanchangCity("রংপুর", 25.7439, 89.2752),
    PanchangCity("ময়মনসিংহ", 24.7471, 90.4203)
)

private fun createPanchangCalendar(
    year: Int,
    month: Int,
    day: Int
): Calendar {
    return Calendar.getInstance(
        TimeZone.getTimeZone("Asia/Dhaka"),
        Locale("bn", "BD")
    ).apply {
        set(year, month - 1, day, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

@Composable
fun PanchangScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val initialDate = remember {
        Calendar.getInstance(
            TimeZone.getTimeZone("Asia/Dhaka")
        )
    }

    var selectedYear by remember {
        mutableIntStateOf(initialDate.get(Calendar.YEAR))
    }

    var selectedMonth by remember {
        mutableIntStateOf(initialDate.get(Calendar.MONTH) + 1)
    }

    var selectedDay by remember {
        mutableIntStateOf(initialDate.get(Calendar.DAY_OF_MONTH))
    }

    var selectedCity by remember {
        mutableStateOf(panchangCities[0])
    }

    var cityMenuExpanded by remember {
        mutableStateOf(false)
    }

    val formattedDate = remember(
        selectedYear,
        selectedMonth,
        selectedDay
    ) {
        val calendar = createPanchangCalendar(
            selectedYear,
            selectedMonth,
            selectedDay
        )

        SimpleDateFormat(
            "EEEE, dd MMMM yyyy",
            Locale("bn", "BD")
        ).apply {
            timeZone = TimeZone.getTimeZone("Asia/Dhaka")
        }.format(calendar.time)
    }

    val panchangResult = remember(
        selectedYear,
        selectedMonth,
        selectedDay,
        selectedCity
    ) {
        runCatching {
            PanchangCalculator.calculate(
                year = selectedYear,
                month = selectedMonth,
                day = selectedDay,
                latitude = selectedCity.latitude,
                longitude = selectedCity.longitude
            )
        }
    }

    val panchangData = panchangResult.getOrNull()

    fun changeDate(amount: Int) {
        val calendar = createPanchangCalendar(
            selectedYear,
            selectedMonth,
            selectedDay
        )

        calendar.add(Calendar.DAY_OF_MONTH, amount)

        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH) + 1
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun selectToday() {
        val today = Calendar.getInstance(
            TimeZone.getTimeZone("Asia/Dhaka")
        )

        selectedYear = today.get(Calendar.YEAR)
        selectedMonth = today.get(Calendar.MONTH) + 1
        selectedDay = today.get(Calendar.DAY_OF_MONTH)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PanchangCream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Header
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "পঞ্জিকা",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = PanchangNavy
            )

            Text(
                text = "তিথি, নক্ষত্র ও দৈনিক পঞ্জিকার তথ্য",
                fontSize = 14.sp,
                color = PanchangMuted
            )
        }

        // Date selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = PanchangWhite
            ),
            border = BorderStroke(1.dp, PanchangBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "তারিখ নির্বাচন",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PanchangText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { changeDate(-1) },
                        contentPadding = PaddingValues(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
                    ) {
                        Text("‹", fontSize = 22.sp)
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formattedDate,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PanchangNavy,
                            textAlign = TextAlign.Center
                        )

                        TextButton(
                            onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        selectedYear = year
                                        selectedMonth = month + 1
                                        selectedDay = day
                                    },
                                    selectedYear,
                                    selectedMonth - 1,
                                    selectedDay
                                ).show()
                            }
                        ) {
                            Text(
                                "ক্যালেন্ডার খুলুন",
                                color = PanchangGold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { changeDate(1) },
                        contentPadding = PaddingValues(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
                    ) {
                        Text("›", fontSize = 22.sp)
                    }
                }

                OutlinedButton(
                    onClick = { selectToday() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("আজকের দিন")
                }
            }
        }

        // City selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = PanchangWhite
            ),
            border = BorderStroke(1.dp, PanchangBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "আপনার অবস্থান",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PanchangText
                )

                Box {
                    OutlinedButton(
                        onClick = {
                            cityMenuExpanded = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedCity.name,
                            color = PanchangNavy
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text("⌄", color = PanchangNavy)
                    }

                    DropdownMenu(
                        expanded = cityMenuExpanded,
                        onDismissRequest = {
                            cityMenuExpanded = false
                        },
                        modifier = Modifier.background(PanchangWhite)
                    ) {
                        panchangCities.forEach { city ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = city.name,
                                        color = PanchangText
                                    )
                                },
                                onClick = {
                                    selectedCity = city
                                    cityMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "অবস্থান পরিবর্তন করলে সূর্যোদয় ও সূর্যাস্তের হিসাবও পরিবর্তিত হবে।",
                    fontSize = 12.sp,
                    color = PanchangMuted
                )
            }
        }

        // Calculation result
        if (panchangData != null) {

            // Main Tithi card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PanchangNavy
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "আজকের তিথি",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Text(
                        text = panchangData.tithi,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = panchangData.paksha,
                        fontSize = 16.sp,
                        color = Color(0xFFE7D4A9)
                    )
                }
            }

            // Panchang information
            Text(
                text = "পঞ্জিকার বিবরণ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PanchangNavy
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PanchangInfoCard(
                    title = "নক্ষত্র",
                    value = panchangData.nakshatra,
                    modifier = Modifier.weight(1f)
                )

                PanchangInfoCard(
                    title = "যোগ",
                    value = panchangData.yoga,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PanchangInfoCard(
                    title = "করণ",
                    value = panchangData.karana,
                    modifier = Modifier.weight(1f)
                )

                PanchangInfoCard(
                    title = "পক্ষ",
                    value = panchangData.paksha,
                    modifier = Modifier.weight(1f)
                )
            }

            // Sunrise and sunset
            Text(
                text = "সূর্যের সময়",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PanchangNavy
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PanchangInfoCard(
                    title = "সূর্যোদয়",
                    value = panchangData.sunrise,
                    modifier = Modifier.weight(1f)
                )

                PanchangInfoCard(
                    title = "সূর্যাস্ত",
                    value = panchangData.sunset,
                    modifier = Modifier.weight(1f)
                )
            }

            // Calculation note
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PanchangSoftBlue
                )
            ) {
                Text(
                    text = panchangData.calculationNote,
                    modifier = Modifier.padding(14.dp),
                    fontSize = 12.sp,
                    color = PanchangNavy,
                    textAlign = TextAlign.Center
                )
            }

        } else {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PanchangWhite
                ),
                border = BorderStroke(1.dp, PanchangBorder)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "হিসাব করা যায়নি",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PanchangNavy
                    )

                    Text(
                        text = "এই তারিখের পঞ্জিকার তথ্য গণনা করতে সমস্যা হয়েছে।",
                        fontSize = 13.sp,
                        color = PanchangMuted,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = panchangResult.exceptionOrNull()
                            ?.message ?: "অজানা সমস্যা",
                        fontSize = 11.sp,
                        color = Color(0xFF9B4141),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun PanchangInfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PanchangWhite
        ),
        border = BorderStroke(1.dp, PanchangBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = PanchangMuted
            )

            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = PanchangNavy,
                textAlign = TextAlign.Center
            )
        }
    }
}


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

private val PCream = Color(0xFFF8F6F0)
private val PWhite = Color.White
private val PNavy = Color(0xFF244B70)
private val PText = Color(0xFF282D32)
private val PMuted = Color(0xFF777D83)
private val PBorder = Color(0xFFE9E5DC)
private val PBlue = Color(0xFFEAF0F6)
private val PGold = Color(0xFF9B7842)

private data class PCity(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

private val pCities = listOf(
    PCity("ঢাকা", 23.8103, 90.4125),
    PCity("সিলেট", 24.8949, 91.8687),
    PCity("চট্টগ্রাম", 22.3569, 91.7832),
    PCity("রাজশাহী", 24.3745, 88.6042),
    PCity("খুলনা", 22.8456, 89.5403),
    PCity("বরিশাল", 22.7010, 90.3535),
    PCity("রংপুর", 25.7439, 89.2752),
    PCity("ময়মনসিংহ", 24.7471, 90.4203)
)

@Composable
fun PanchangScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val today = remember {
        Calendar.getInstance(
            TimeZone.getTimeZone("Asia/Dhaka")
        )
    }

    var year by remember {
        mutableIntStateOf(today.get(Calendar.YEAR))
    }
    var month by remember {
        mutableIntStateOf(today.get(Calendar.MONTH) + 1)
    }
    var day by remember {
        mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH))
    }

    var cityIndex by remember {
        mutableIntStateOf(0)
    }

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    val city = pCities[cityIndex]

    val dateText = remember(year, month, day) {
        val calendar = Calendar.getInstance(
            TimeZone.getTimeZone("Asia/Dhaka"),
            Locale("bn", "BD")
        )
        calendar.set(year, month - 1, day, 12, 0, 0)

        SimpleDateFormat(
            "EEEE, dd MMMM yyyy",
            Locale("bn", "BD")
        ).apply {
            timeZone = TimeZone.getTimeZone("Asia/Dhaka")
        }.format(calendar.time)
    }

    val result = remember(year, month, day, cityIndex) {
        runCatching {
            PanchangCalculator.calculate(
                year = year,
                month = month,
                day = day,
                latitude = city.latitude,
                longitude = city.longitude
            )
        }
    }

    val data = result.getOrNull()

    fun updateDate(amount: Int) {
        val calendar = Calendar.getInstance(
            TimeZone.getTimeZone("Asia/Dhaka")
        )
        calendar.set(year, month - 1, day, 12, 0, 0)
        calendar.add(Calendar.DAY_OF_MONTH, amount)

        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PCream)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "পঞ্জিকা",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = PNavy
        )

        Text(
            text = "তিথি, নক্ষত্র ও দৈনিক পঞ্জিকার তথ্য",
            fontSize = 14.sp,
            color = PMuted
        )

        // Date card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = PWhite
            ),
            border = BorderStroke(1.dp, PBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "তারিখ নির্বাচন",
                    fontWeight = FontWeight.SemiBold,
                    color = PText
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { updateDate(-1) }
                    ) {
                        Text("‹", fontSize = 22.sp)
                    }

                    Text(
                        text = dateText,
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PNavy,
                        textAlign = TextAlign.Center
                    )

                    OutlinedButton(
                        onClick = { updateDate(1) }
                    ) {
                        Text("›", fontSize = 22.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                year = selectedYear
                                month = selectedMonth + 1
                                day = selectedDay
                            },
                            year,
                            month - 1,
                            day
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ক্যালেন্ডার থেকে তারিখ নিন")
                }

                TextButton(
                    onClick = {
                        year = today.get(Calendar.YEAR)
                        month = today.get(Calendar.MONTH) + 1
                        day = today.get(Calendar.DAY_OF_MONTH)
                    }
                ) {
                    Text("আজকের দিন", color = PGold)
                }
            }
        }

        // City selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = PWhite
            ),
            border = BorderStroke(1.dp, PBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "আপনার শহর",
                    fontWeight = FontWeight.SemiBold,
                    color = PText
                )

                Spacer(Modifier.height(8.dp))

                Box {
                    OutlinedButton(
                        onClick = {
                            menuExpanded = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(city.name, color = PNavy)
                        Spacer(Modifier.weight(1f))
                        Text("⌄", color = PNavy)
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            menuExpanded = false
                        }
                    ) {
                        pCities.forEachIndexed { index, item ->
                            DropdownMenuItem(
                                text = { Text(item.name) },
                                onClick = {
                                    cityIndex = index
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (data != null) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PNavy
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "আজকের তিথি",
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        data.tithi,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = PWhite
                    )

                    Text(
                        data.paksha,
                        fontSize = 16.sp,
                        color = Color(0xFFE7D4A9)
                    )
                }
            }

            Text(
                "পঞ্জিকার বিবরণ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PNavy
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PanchangValueCard(
                    "নক্ষত্র",
                    data.nakshatra,
                    Modifier.weight(1f)
                )
                PanchangValueCard(
                    "যোগ",
                    data.yoga,
                    Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PanchangValueCard(
                    "করণ",
                    data.karana,
                    Modifier.weight(1f)
                )
                PanchangValueCard(
                    "পক্ষ",
                    data.paksha,
                    Modifier.weight(1f)
                )
            }

            Text(
                "সূর্যের সময়",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PNavy
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PanchangValueCard(
                    "সূর্যোদয়",
                    data.sunrise,
                    Modifier.weight(1f)
                )
                PanchangValueCard(
                    "সূর্যাস্ত",
                    data.sunset,
                    Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PBlue
                )
            ) {
                Text(
                    text = data.calculationNote,
                    modifier = Modifier.padding(14.dp),
                    fontSize = 12.sp,
                    color = PNavy,
                    textAlign = TextAlign.Center
                )
            }

        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PWhite
                ),
                border = BorderStroke(1.dp, PBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "হিসাব করা যায়নি",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PNavy
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        result.exceptionOrNull()?.message
                            ?: "অজানা সমস্যা",
                        fontSize = 12.sp,
                        color = Color(0xFF9B4141),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PanchangValueCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PWhite
        ),
        border = BorderStroke(1.dp, PBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                fontSize = 13.sp,
                color = PMuted
            )

            Spacer(Modifier.height(8.dp))

            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PNavy,
                textAlign = TextAlign.Center
            )
        }
    }
}


package com.unlogistix.tithi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val MCream = Color(0xFFF8F6F0)
private val MWhite = Color.White
private val MNavy = Color(0xFF244B70)
private val MText = Color(0xFF282D32)
private val MMuted = Color(0xFF777D83)
private val MBorder = Color(0xFFE9E5DC)
private val MGold = Color(0xFF9B7842)
private val MSoftBlue = Color(0xFFEAF0F6)

private val bengaliMonths = listOf(
    "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল",
    "মে", "জুন", "জুলাই", "আগস্ট",
    "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
)

private val bengaliWeekdays = listOf(
    "রবি", "সোম", "মঙ্গল", "বুধ",
    "বৃহস্পতি", "শুক্র", "শনি"
)

private fun createMonthCalendar(
    year: Int,
    month: Int
): Calendar {
    return Calendar.getInstance(
        TimeZone.getTimeZone("Asia/Dhaka"),
        Locale("bn", "BD")
    ).apply {
        clear()
        set(year, month, 1, 12, 0, 0)
    }
}

@Composable
fun MonthlyCalendarScreenContent(
    modifier: Modifier = Modifier
) {
    val today = remember {
        Calendar.getInstance(
            TimeZone.getTimeZone("Asia/Dhaka")
        )
    }

    var displayedYear by remember {
        mutableIntStateOf(today.get(Calendar.YEAR))
    }

    var displayedMonth by remember {
        mutableIntStateOf(today.get(Calendar.MONTH))
    }

    var selectedDay by remember {
        mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH))
    }

    val todayYear = today.get(Calendar.YEAR)
    val todayMonth = today.get(Calendar.MONTH)
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    val daysInMonth = remember(
        displayedYear,
        displayedMonth
    ) {
        createMonthCalendar(
            displayedYear,
            displayedMonth
        ).getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstWeekday = remember(
        displayedYear,
        displayedMonth
    ) {
        createMonthCalendar(
            displayedYear,
            displayedMonth
        ).get(Calendar.DAY_OF_WEEK) - 1
    }

    val calendarDays = remember(
        displayedYear,
        displayedMonth,
        daysInMonth,
        firstWeekday
    ) {
        val cells = mutableListOf<Int?>()

        repeat(firstWeekday) {
            cells.add(null)
        }

        for (day in 1..daysInMonth) {
            cells.add(day)
        }

        while (cells.size % 7 != 0) {
            cells.add(null)
        }

        cells
    }

    val selectedDateText = remember(
        displayedYear,
        displayedMonth,
        selectedDay
    ) {
        val calendar = createMonthCalendar(
            displayedYear,
            displayedMonth
        )

        calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

        val formatter = java.text.SimpleDateFormat(
            "EEEE, dd MMMM yyyy",
            Locale("bn", "BD")
        )

        formatter.timeZone = TimeZone.getTimeZone("Asia/Dhaka")
        formatter.format(calendar.time)
    }

    fun changeMonth(amount: Int) {
        val calendar = createMonthCalendar(
            displayedYear,
            displayedMonth
        )

        calendar.add(Calendar.MONTH, amount)

        displayedYear = calendar.get(Calendar.YEAR)
        displayedMonth = calendar.get(Calendar.MONTH)
        selectedDay = 1
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MCream)
            .padding(18.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "মাসিক ক্যালেন্ডার",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MNavy
        )

        Text(
            text = "আপনার পছন্দের তারিখ নির্বাচন করুন",
            fontSize = 14.sp,
            color = MMuted
        )

        // Month navigation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MWhite
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { changeMonth(-1) }
                ) {
                    Text("‹", fontSize = 22.sp)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = bengaliMonths[displayedMonth],
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = MNavy
                    )

                    Text(
                        text = displayedYear.toString(),
                        fontSize = 14.sp,
                        color = MMuted
                    )
                }

                OutlinedButton(
                    onClick = { changeMonth(1) }
                ) {
                    Text("›", fontSize = 22.sp)
                }
            }
        }

        // Calendar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MWhite
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    bengaliWeekdays.forEach { weekday ->
                        Text(
                            text = weekday,
                            modifier = Modifier.weight(1f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MGold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                HorizontalDivider(color = MBorder)

                calendarDays.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        week.forEach { day ->

                            val isSelected = day == selectedDay
                            val isToday = day != null &&
                                displayedYear == todayYear &&
                                displayedMonth == todayMonth &&
                                day == todayDay

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.85f)
                                    .padding(2.dp)
                                    .background(
                                        color = when {
                                            isSelected -> MNavy
                                            isToday -> MSoftBlue
                                            else -> Color.Transparent
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .then(
                                        if (day != null) {
                                            Modifier.clickable {
                                                selectedDay = day
                                            }
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day != null) {
                                    Text(
                                        text = day.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (
                                            isSelected || isToday
                                        ) FontWeight.Bold
                                        else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MWhite
                                            isToday -> MNavy
                                            else -> MText
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected date
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MWhite
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "নির্বাচিত তারিখ",
                    fontSize = 14.sp,
                    color = MMuted
                )

                Text(
                    text = selectedDateText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MNavy
                )

                if (
                    displayedYear == todayYear &&
                    displayedMonth == todayMonth &&
                    selectedDay == todayDay
                ) {
                    Text(
                        text = "আজকের দিন",
                        fontSize = 12.sp,
                        color = MGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = {
                        displayedYear = todayYear
                        displayedMonth = todayMonth
                        selectedDay = todayDay
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("আজকের তারিখে ফিরে যান")
                }
            }
        }

        Text(
            text = "নোট: এটি আপাতত সাধারণ মাসিক ক্যালেন্ডার। তিথি ও উৎসবের তথ্য পরবর্তী ধাপে যুক্ত হবে।",
            fontSize = 12.sp,
            color = MMuted
        )
    }
}

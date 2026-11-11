
package com.unlogistix.tithi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val Cream = Color(0xFFF8F6F0)
private val White = Color(0xFFFFFFFF)
private val Navy = Color(0xFF244B70)
private val TextDark = Color(0xFF282D32)
private val Muted = Color(0xFF777D83)
private val SoftBlue = Color(0xFFEAF0F6)
private val Border = Color(0xFFE9E5DC)
private val Gold = Color(0xFF9B7842)

private val DhakaTimeZone = TimeZone.getTimeZone("Asia/Dhaka")
private val BengaliLocale = Locale("bn", "BD")

private data class ScriptureVerse(
    val sanskrit: String,
    val meaningBn: String,
    val reference: String
)

private val dailyVerses = listOf(
    ScriptureVerse(
        sanskrit = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।\n" +
            "मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥",
        meaningBn = "কর্ম করাই তোমার অধিকার, তার ফলে কখনোই নয়। " +
            "কর্মফলকে উদ্দেশ্য করে কাজ করো না, আবার কর্ম না করার প্রতিও আসক্ত হয়ো না।",
        reference = "শ্রীমদ্ভগবদ্গীতা · ২.৪৭"
    ),
    ScriptureVerse(
        sanskrit = "योगस्थः कुरु कर्माणि सङ्गं त्यक्त्वा धनञ्जय।\n" +
            "सिद्ध्यसिद्ध्योः समो भूत्वा समत्वं योग उच्यते॥",
        meaningBn = "হে ধনঞ্জয়, আসক্তি ত্যাগ করে যোগে প্রতিষ্ঠিত হয়ে কর্ম করো। " +
            "সাফল্য ও ব্যর্থতায় সমভাব বজায় রাখো; এই সমভাবকেই যোগ বলা হয়।",
        reference = "শ্রীমদ্ভগবদ্গীতা · ২.৪৮"
    ),
    ScriptureVerse(
        sanskrit = "यस्मान्नोद्विजते लोको लोकान्नोद्विजते च यः।\n" +
            "हर्षामर्षभयोद्वेगैर्मुक्तो यः स च मे प्रियः॥",
        meaningBn = "যাঁর দ্বারা জগৎ বিচলিত হয় না এবং যিনি জগতের দ্বারা বিচলিত হন না, " +
            "যিনি আনন্দ, ঈর্ষা, ভয় ও উদ্বেগ থেকে মুক্ত—তিনি আমার প্রিয়।",
        reference = "শ্রীমদ্ভগবদ্গীতা · ১২.১৫"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TithiApp()
        }
    }
}

@Composable
fun TithiApp() {
    var selectedTab by remember { mutableIntStateOf(0) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Cream,
            surface = White,
            primary = Navy,
            onBackground = TextDark,
            onSurface = TextDark
        )
    ) {
        Scaffold(
            containerColor = Cream,
            bottomBar = {
                NavigationBar(
                    containerColor = White,
                    contentColor = Navy
                ) {
                    val items = listOf(
                        "হোম",
                        "পঞ্জিকা",
                        "জ্ঞান",
                        "আমার দিন"
                    )

                    val symbols = listOf("⌂", "◷", "✧", "☷")

                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Text(symbols[index], fontSize = 21.sp)
                            },
                            label = {
                                Text(item, fontSize = 11.sp)
                            }
                        )
                    }
                }
            }
        ) { padding ->
            when (selectedTab) {
                0 -> HomeScreen(Modifier.padding(padding))
                1 -> PanchangScreen(Modifier.padding(padding))
                2 -> InfoScreen(
                    Modifier.padding(padding),
                    "জ্ঞানভাণ্ডার",
                    "হিন্দু ধর্ম, দেবদেবী, ঋষি, শাস্ত্র ও দর্শন সম্পর্কে যাচাইকৃত জ্ঞান এখানে সাজানো হবে।"
                )
                3 -> MyDayScreen(
                    Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "তিথি",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
                Text(
                    "T I T H I",
                    fontSize = 11.sp,
                    color = Muted,
                    fontWeight = FontWeight.Medium
                )
            }

            Text("ॐ", fontSize = 34.sp, color = Navy)
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "নমস্কার",
            fontSize = 25.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )

        Spacer(Modifier.height(5.dp))

        Text(
            "প্রতিদিনের সময়, জ্ঞান ও জীবনের সঙ্গী।",
            fontSize = 14.sp,
            color = Muted
        )

        Spacer(Modifier.height(24.dp))

        SectionHeading("আজকের পঞ্জিকা", "DAILY PANCHANG")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            border = BorderStroke(1.dp, Border)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "আজকের তিথি",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Navy
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "পঞ্জিকার নির্ভরযোগ্য তথ্য প্রস্তুত করা হচ্ছে।",
                    fontSize = 13.sp,
                    color = Muted
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = Border
                )

                InfoRow("তিথি", "তথ্য অপেক্ষমাণ")
                InfoRow("নক্ষত্র", "তথ্য অপেক্ষমাণ")
                InfoRow("পক্ষ", "তথ্য অপেক্ষমাণ")
                InfoRow("যোগ ও করণ", "তথ্য অপেক্ষমাণ")
                InfoRow("সূর্যোদয় ও সূর্যাস্ত", "তথ্য অপেক্ষমাণ")
            }
        }

        Spacer(Modifier.height(24.dp))

        SectionHeading("আজকের বিশেষ", "TODAY'S HIGHLIGHTS")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SoftBlue)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "উৎসব ও ব্রত",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Navy
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    "উৎসব, একাদশী ও পারণের যাচাইকৃত তথ্য এখানে দেখানো হবে।",
                    fontSize = 13.sp,
                    color = TextDark
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        SectionHeading("আজকের শাস্ত্রবাণী", "DAILY SCRIPTURE")

        DailyScriptureCard()

        Spacer(Modifier.height(28.dp))

        Text(
            "TITHI • জ্ঞান, ঐতিহ্য ও আত্মিক চর্চা",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 11.sp,
            color = Muted
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun DailyScriptureCard() {
    val verse = remember {
        val dayOfYear = Calendar.getInstance(
            DhakaTimeZone
        ).get(Calendar.DAY_OF_YEAR)

        dailyVerses[(dayOfYear - 1) % dailyVerses.size]
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "✦  श्रीमद्भगवद्गीता  ✦",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Gold
            )

            Spacer(Modifier.height(18.dp))

            Text(
                verse.sanskrit,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Medium,
                color = Navy,
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 18.dp),
                color = Border
            )

            Text(
                "বাংলা অর্থ",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gold
            )

            Spacer(Modifier.height(9.dp))

            Text(
                verse.meaningBn,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                color = TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                verse.reference,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Navy
            )
        }
    }
}

@Composable
fun PanchangScreen(modifier: Modifier = Modifier) {
    var selectedDateMillis by remember {
        mutableLongStateOf(
            Calendar.getInstance(DhakaTimeZone).timeInMillis
        )
    }

    val dateFormatter = remember {
        SimpleDateFormat(
            "EEEE, d MMMM yyyy",
            BengaliLocale
        ).apply {
            timeZone = DhakaTimeZone
        }
    }

    val selectedDateText = remember(selectedDateMillis) {
        dateFormatter.format(Date(selectedDateMillis))
    }

    val isToday = remember(selectedDateMillis) {
        val selected = Calendar.getInstance(DhakaTimeZone)
        selected.timeInMillis = selectedDateMillis

        val today = Calendar.getInstance(DhakaTimeZone)

        selected.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            selected.get(Calendar.DAY_OF_YEAR) ==
            today.get(Calendar.DAY_OF_YEAR)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(22.dp))

        Text(
            "TITHI",
            fontSize = 11.sp,
            color = Muted,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(7.dp))

        Text(
            "দৈনিক পঞ্জিকা",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Navy
        )

        Spacer(Modifier.height(5.dp))

        Text(
            "প্রতিদিনের পঞ্জিকার বিস্তারিত তথ্য",
            fontSize = 13.sp,
            color = Muted
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            border = BorderStroke(1.dp, Border)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "নির্বাচিত তারিখ",
                    fontSize = 12.sp,
                    color = Muted
                )

                Spacer(Modifier.height(7.dp))

                Text(
                    selectedDateText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Navy
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val calendar =
                                Calendar.getInstance(DhakaTimeZone)
                            calendar.timeInMillis = selectedDateMillis
                            calendar.add(Calendar.DAY_OF_MONTH, -1)
                            selectedDateMillis = calendar.timeInMillis
                        }
                    ) {
                        Text("← আগের দিন")
                    }

                    if (!isToday) {
                        TextButton(
                            onClick = {
                                selectedDateMillis =
                                    Calendar.getInstance(
                                        DhakaTimeZone
                                    ).timeInMillis
                            }
                        ) {
                            Text("আজ")
                        }
                    }

                    TextButton(
                        onClick = {
                            val calendar =
                                Calendar.getInstance(DhakaTimeZone)
                            calendar.timeInMillis = selectedDateMillis
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                            selectedDateMillis = calendar.timeInMillis
                        }
                    ) {
                        Text("পরের দিন →")
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        SectionHeading("তিথি ও পক্ষ", "TITHI & PAKSHA")

        PanchangInfoCard(
            "চন্দ্রতিথির বিবরণ",
            listOf(
                "তিথি" to "তথ্য অপেক্ষমাণ",
                "পক্ষ" to "তথ্য অপেক্ষমাণ"
            )
        )

        Spacer(Modifier.height(20.dp))

        SectionHeading("নক্ষত্র ও যোগ", "NAKSHATRA & YOGA")

        PanchangInfoCard(
            "জ্যোতিষীয় বিবরণ",
            listOf(
                "নক্ষত্র" to "তথ্য অপেক্ষমাণ",
                "যোগ" to "তথ্য অপেক্ষমাণ",
                "করণ" to "তথ্য অপেক্ষমাণ"
            )
        )

        Spacer(Modifier.height(20.dp))

        SectionHeading("সূর্যের সময়", "SUNRISE & SUNSET")

        PanchangInfoCard(
            "স্থানীয় সূর্যোদয় ও সূর্যাস্ত",
            listOf(
                "সূর্যোদয়" to "তথ্য অপেক্ষমাণ",
                "সূর্যাস্ত" to "তথ্য অপেক্ষমাণ"
            )
        )

        Spacer(Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftBlue)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "তথ্যের নির্ভরযোগ্যতা",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Navy
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "এই সংস্করণে পঞ্জিকার গণনা এখনো যুক্ত হয়নি। " +
                        "যাচাই ছাড়া কোনো তিথি, সময় বা জ্যোতিষীয় তথ্য দেখানো হচ্ছে না।",
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    color = TextDark
                )
            }
        }

        Spacer(Modifier.height(26.dp))
    }
}

@Composable
fun PanchangInfoCard(
    title: String,
    rows: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Navy
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Border
            )

            rows.forEach { (label, value) ->
                InfoRow(label, value)
            }
        }
    }
}

@Composable
fun SectionHeading(title: String, subtitle: String) {
    Column {
        Text(
            subtitle,
            fontSize = 10.sp,
            color = Muted,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }

    Spacer(Modifier.height(12.dp))
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = Muted
        )

        Text(
            value,
            fontSize = 12.sp,
            color = TextDark,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    title: String,
    description: String
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .padding(22.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            "TITHI",
            fontSize = 12.sp,
            color = Muted,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(10.dp))

        Text(
            title,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Navy
        )

        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            border = BorderStroke(1.dp, Border)
        ) {
            Text(
                description,
                modifier = Modifier.padding(20.dp),
                fontSize = 15.sp,
                lineHeight = 25.sp,
                color = TextDark
            )
        }
    }
}

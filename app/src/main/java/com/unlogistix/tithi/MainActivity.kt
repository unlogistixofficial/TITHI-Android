
package com.unlogistix.tithi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Cream = Color(0xFFF8F6F0)
private val White = Color(0xFFFFFFFF)
private val Navy = Color(0xFF244B70)
private val TextDark = Color(0xFF282D32)
private val Muted = Color(0xFF777D83)
private val SoftBlue = Color(0xFFEAF0F6)
private val Border = Color(0xFFE9E5DC)

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
                                Text(
                                    text = symbols[index],
                                    fontSize = 21.sp
                                )
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
                1 -> InfoScreen(
                    Modifier.padding(padding),
                    "পঞ্জিকা",
                    "প্রতিদিনের তিথি, পক্ষ, নক্ষত্র, যোগ ও করণের নির্ভরযোগ্য তথ্য এখানে যুক্ত করা হবে।"
                )
                2 -> InfoScreen(
                    Modifier.padding(padding),
                    "জ্ঞানভাণ্ডার",
                    "হিন্দু ধর্ম, দেবদেবী, ঋষি, শাস্ত্র ও দর্শন সম্পর্কে যাচাইকৃত জ্ঞান এখানে সাজানো হবে।"
                )
                3 -> InfoScreen(
                    Modifier.padding(padding),
                    "আমার দিন",
                    "তোমার দৈনন্দিন কাজ, অভ্যাস ও স্মরণিকা এখানে পরিচালনা করতে পারবে।"
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

            Text(
                "ॐ",
                fontSize = 34.sp,
                color = Navy
            )
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
            border = androidx.compose.foundation.BorderStroke(1.dp, Border)
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

        SectionHeading("জ্ঞান ও অনুপ্রেরণা", "WISDOM")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Border)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "শ্রীমদ্ভগবদ্গীতা",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Navy
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "জ্ঞান, কর্ম ও ভক্তির আলোকে নিজেকে জানার এক চিরন্তন শিক্ষা।",
                    fontSize = 13.sp,
                    color = Muted
                )
            }
        }

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
            colors = CardDefaults.cardColors(containerColor = White)
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

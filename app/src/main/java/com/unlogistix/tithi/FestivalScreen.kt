
package com.unlogistix.tithi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FestivalNavy = Color(0xFF263B59)
private val FestivalCream = Color(0xFFFAF8F2)
private val FestivalGold = Color(0xFFB79555)

private data class Festival(
    val name: String,
    val month: String,
    val description: String
)

private val festivalList = listOf(
    Festival("মকর সংক্রান্তি", "জানুয়ারি", "সূর্যের মকর রাশিতে প্রবেশ উপলক্ষে পালিত উৎসব।"),
    Festival("বসন্ত পঞ্চমী", "জানুয়ারি / ফেব্রুয়ারি", "দেবী সরস্বতীর আরাধনার বিশেষ দিন।"),
    Festival("মহাশিবরাত্রি", "ফেব্রুয়ারি / মার্চ", "ভগবান শিবের উপাসনায় পালিত পবিত্র রাত্রি।"),
    Festival("দোল পূর্ণিমা", "মার্চ", "রঙ, আনন্দ ও ভক্তির উৎসব।"),
    Festival("রাম নবমী", "মার্চ / এপ্রিল", "ভগবান শ্রীরামের জন্মতিথি হিসেবে পালিত হয়।"),
    Festival("হনুমান জয়ন্তী", "এপ্রিল", "ভগবান হনুমানের জন্মোৎসব।"),
    Festival("রথযাত্রা", "জুন / জুলাই", "ভগবান জগন্নাথ, বলরাম ও সুভদ্রার রথযাত্রা।"),
    Festival("জন্মাষ্টমী", "আগস্ট", "ভগবান শ্রীকৃষ্ণের জন্মোৎসব।"),
    Festival("গণেশ চতুর্থী", "আগস্ট / সেপ্টেম্বর", "ভগবান গণেশের আরাধনার উৎসব।"),
    Festival("মহালয়া", "সেপ্টেম্বর / অক্টোবর", "দেবীপক্ষের সূচনার সঙ্গে যুক্ত বিশেষ দিন।"),
    Festival("দুর্গাপূজা", "সেপ্টেম্বর / অক্টোবর", "দেবী দুর্গার আরাধনার প্রধান উৎসব।"),
    Festival("লক্ষ্মীপূজা", "অক্টোবর", "দেবী লক্ষ্মীর আরাধনা ও সমৃদ্ধির প্রার্থনা।"),
    Festival("কালীপূজা", "অক্টোবর / নভেম্বর", "দেবী কালীর উপাসনায় পালিত উৎসব।"),
    Festival("দীপাবলি", "অক্টোবর / নভেম্বর", "আলোর উৎসব, শুভশক্তির জয়ের প্রতীক।"),
    Festival("কার্তিক পূর্ণিমা", "নভেম্বর", "কার্তিক মাসের পূর্ণিমায় পালিত পবিত্র তিথি।")
)

@Composable
fun FestivalScreen(modifier: Modifier = Modifier) {

    var selectedFestival by remember { mutableStateOf<Festival?>(null) }
    var search by remember { mutableStateOf("") }

    val filtered = festivalList.filter {
        it.name.contains(search, ignoreCase = true) ||
        it.month.contains(search, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FestivalCream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    FestivalNavy,
                    RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp)
                )
                .padding(22.dp)
        ) {
            Text(
                "উৎসব ক্যালেন্ডার",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(7.dp))

            Text(
                "সনাতন ধর্মের ব্রত, পূজা ও উৎসব",
                color = Color(0xFFD8DFE8),
                fontSize = 13.sp
            )

            Spacer(Modifier.height(18.dp))

            Text(
                "ঐতিহ্য • ভক্তি • আনন্দ",
                color = FestivalGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("উৎসব বা মাস খুঁজুন...") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FestivalNavy,
                unfocusedBorderColor = Color(0xFFE2DED5),
                cursorColor = FestivalNavy
            )
        )

        Spacer(Modifier.height(14.dp))

        if (selectedFestival == null) {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 6.dp
                ),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                item {
                    Text(
                        "উৎসব ও বিশেষ দিন",
                        color = FestivalNavy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(filtered) { festival ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFestival = festival },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    festival.month,
                                    color = FestivalGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    festival.name,
                                    color = FestivalNavy,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    festival.description,
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )
                            }
                            Text(
                                "›",
                                color = FestivalGold,
                                fontSize = 28.sp
                            )
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    item {
                        Text(
                            "কোনো উৎসব পাওয়া যায়নি।",
                            color = Color.Gray,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }

                item {
                    Text(
                        "দ্রষ্টব্য: এখানে মাসগুলো আনুমানিক। প্রকৃত উৎসবের তারিখ পঞ্জিকা, স্থান ও প্রচলিত সিদ্ধান্ত অনুযায়ী নির্ধারিত হবে।",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TextButton(
                    onClick = { selectedFestival = null }
                ) {
                    Text("← ফিরে যান", color = FestivalNavy)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text(
                            selectedFestival!!.month,
                            color = FestivalGold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            selectedFestival!!.name,
                            color = FestivalNavy,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 18.dp),
                            color = Color(0xFFEAE5DA)
                        )
                        Text(
                            selectedFestival!!.description,
                            color = Color.DarkGray,
                            fontSize = 16.sp,
                            lineHeight = 27.sp
                        )
                    }
                }
            }
        }
    }
}

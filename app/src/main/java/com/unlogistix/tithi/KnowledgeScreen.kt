
package com.unlogistix.tithi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val KnowledgeNavy = Color(0xFF263B59)
private val KnowledgeCream = Color(0xFFFAF8F2)
private val KnowledgeGold = Color(0xFFB79555)
private val KnowledgeText = Color(0xFF30343B)
private val KnowledgeMuted = Color(0xFF777777)

private data class KnowledgeItem(
    val title: String,
    val category: String,
    val description: String,
    val details: String
)

private val knowledgeItems = listOf(
    KnowledgeItem(
        "সনাতন ধর্মের মূল ভাবনা",
        "ধর্মজ্ঞান",
        "সত্য, ধর্ম, কর্ম ও মানবকল্যাণ সনাতন দর্শনের গুরুত্বপূর্ণ ভাবনা।",
        "সনাতন ধর্মে সত্য, ন্যায়, আত্মসংযম, দয়া ও কর্তব্যবোধকে বিশেষ গুরুত্ব দেওয়া হয়। মানুষের জীবনকে সুন্দর ও অর্থপূর্ণ করে তোলাই ধর্মাচরণের অন্যতম উদ্দেশ্য।"
    ),
    KnowledgeItem(
        "ভগবদ্গীতার কর্মযোগ",
        "ধর্মজ্ঞান",
        "কর্ম করে যাও, কিন্তু ফলের প্রতি অতিরিক্ত আসক্ত হয়ো না।",
        "ভগবদ্গীতার কর্মযোগ মানুষকে নিজের কর্তব্য নিষ্ঠার সঙ্গে পালন করতে শেখায়। কর্মের ফলের চিন্তায় অস্থির না হয়ে সৎভাবে কাজ করাই এর মূল শিক্ষা।"
    ),
    KnowledgeItem(
        "ওঁ (ॐ) মন্ত্র",
        "মন্ত্র",
        "ওঁ-কে হিন্দু আধ্যাত্মিক ঐতিহ্যে পবিত্র ধ্বনি হিসেবে মানা হয়।",
        "ওঁ বা প্রণবকে বহু উপনিষদ ও হিন্দু আধ্যাত্মিক ধারায় গভীর তাৎপর্যপূর্ণ ধ্বনি হিসেবে বর্ণনা করা হয়েছে। ধ্যান ও প্রার্থনার শুরুতে অনেকেই এই ধ্বনি উচ্চারণ করেন।"
    ),
    KnowledgeItem(
        "গায়ত্রী মন্ত্র",
        "মন্ত্র",
        "আলোক, জ্ঞান ও বুদ্ধির সৎপথের জন্য প্রার্থনা।",
        "ॐ ভূর্ভুবঃ স্বঃ। তৎ সবিতুর্বরেণ্যং। ভর্গো দেবস্য ধীমহি। ধিয়ো যো নঃ প্রচোদয়াৎ॥\n\nএই মন্ত্রটি ঋগ্বেদের একটি প্রসিদ্ধ মন্ত্র। এর ভাবার্থ হলো—আমরা সেই দিব্য আলোর ধ্যান করি, যা আমাদের বুদ্ধিকে সৎপথে পরিচালিত করুক।"
    ),
    KnowledgeItem(
        "একাদশী ব্রত",
        "ব্রত-পূজা",
        "হিন্দু ঐতিহ্যে একাদশী তিথি ভগবান বিষ্ণুর উপাসনার সঙ্গে যুক্ত।",
        "প্রতি চান্দ্রপক্ষের একাদশী তিথিতে বহু ভক্ত উপবাস, প্রার্থনা ও ভগবান বিষ্ণুর স্মরণ করেন। উপবাসের নিয়ম অঞ্চল ও পারিবারিক প্রথা অনুযায়ী ভিন্ন হতে পারে।"
    ),
    KnowledgeItem(
        "মহাশিবরাত্রি",
        "উৎসব",
        "ভগবান শিবের আরাধনায় পালিত একটি গুরুত্বপূর্ণ উৎসব।",
        "মহাশিবরাত্রিতে বহু ভক্ত উপবাস, শিবপূজা, মন্ত্রজপ ও রাত্রিজাগরণ করেন। শিবভক্তদের কাছে দিনটি বিশেষ আধ্যাত্মিক তাৎপর্য বহন করে।"
    ),
    KnowledgeItem(
        "জন্মাষ্টমী",
        "উৎসব",
        "ভগবান শ্রীকৃষ্ণের জন্মোৎসব হিসেবে পালিত হয়।",
        "জন্মাষ্টমীতে ভক্তরা শ্রীকৃষ্ণের পূজা, ভজন, কীর্তন ও ধর্মীয় আলোচনা করেন। বিভিন্ন অঞ্চলে নিজস্ব রীতি অনুযায়ী উৎসবটি পালিত হয়।"
    ),
    KnowledgeItem(
        "দুর্গাপূজা",
        "উৎসব",
        "দেবী দুর্গার আরাধনা ও অশুভের বিরুদ্ধে শুভশক্তির জয়ের উৎসব।",
        "দুর্গাপূজা শক্তি, সাহস ও শুভশক্তির প্রতীক। এই উৎসবকে কেন্দ্র করে পূজা, অঞ্জলি, সাংস্কৃতিক অনুষ্ঠান ও পারিবারিক মিলনমেলা অনুষ্ঠিত হয়।"
    )
)

@Composable
fun KnowledgeScreen(modifier: Modifier = Modifier) {

    var selectedCategory by remember { mutableStateOf("সব") }
    var searchText by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<KnowledgeItem?>(null) }

    val categories = listOf(
        "সব",
        "ধর্মজ্ঞান",
        "মন্ত্র",
        "ব্রত-পূজা",
        "উৎসব"
    )

    val filteredItems = knowledgeItems.filter {
        (selectedCategory == "সব" || it.category == selectedCategory) &&
        (it.title.contains(searchText, ignoreCase = true) ||
         it.description.contains(searchText, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KnowledgeCream)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    KnowledgeNavy,
                    RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp)
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "জ্ঞান ভাণ্ডার",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "সনাতন ধর্ম, আধ্যাত্মিকতা ও ঐতিহ্যের পাঠ",
                color = Color(0xFFD8DFE8),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(15.dp)
                ) {
                    Text(
                        text = "আজকের ভাবনা",
                        color = KnowledgeGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = "নিজের কর্তব্য নিষ্ঠার সঙ্গে পালন করাই জীবনের অন্যতম শিক্ষা।",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 23.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = {
                Text("জ্ঞান, মন্ত্র বা উৎসব খুঁজুন...")
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KnowledgeNavy,
                unfocusedBorderColor = Color(0xFFE2DED5),
                focusedTextColor = KnowledgeText,
                unfocusedTextColor = KnowledgeText,
                cursorColor = KnowledgeNavy
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KnowledgeNavy,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = KnowledgeText
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedItem == null) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {
                    Text(
                        text = "জানুন ও শিখুন",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = KnowledgeNavy
                    )
                }

                if (filteredItems.isEmpty()) {
                    item {
                        Text(
                            text = "কোনো তথ্য পাওয়া যায়নি।",
                            color = KnowledgeMuted,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }

                items(filteredItems) { item ->
                    KnowledgeCard(
                        item = item,
                        onClick = { selectedItem = item }
                    )
                }
            }

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {

                TextButton(
                    onClick = { selectedItem = null },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "← ফিরে যান",
                        color = KnowledgeNavy
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = selectedItem!!.category,
                            color = KnowledgeGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = selectedItem!!.title,
                            color = KnowledgeNavy,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = Color(0xFFEAE5DA)
                        )

                        Text(
                            text = selectedItem!!.description,
                            color = KnowledgeMuted,
                            fontSize = 14.sp,
                            lineHeight = 23.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "বিস্তারিত",
                            color = KnowledgeNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = selectedItem!!.details,
                            color = KnowledgeText,
                            fontSize = 15.sp,
                            lineHeight = 27.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun KnowledgeCard(
    item: KnowledgeItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.category,
                    color = KnowledgeGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.title,
                    color = KnowledgeNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.description,
                    color = KnowledgeMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "›",
                color = KnowledgeGold,
                fontSize = 28.sp
            )
        }
    }
}

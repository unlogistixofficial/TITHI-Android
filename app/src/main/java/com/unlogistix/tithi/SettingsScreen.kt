
package com.unlogistix.tithi

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SettingsCream = Color(0xFFF8F6F0)
private val SettingsWhite = Color(0xFFFFFFFF)
private val SettingsNavy = Color(0xFF244B70)
private val SettingsText = Color(0xFF282D32)
private val SettingsMuted = Color(0xFF777D83)
private val SettingsBorder = Color(0xFFE9E5DC)

private object TithiSettingsStorage {

    private const val PREFS = "tithi_app_settings"
    private const val THEME = "app_theme"
    private const val LANGUAGE = "app_language"

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getTheme(context: Context): String {
        return preferences(context).getString(THEME, "system") ?: "system"
    }

    fun saveTheme(context: Context, value: String) {
        preferences(context).edit().putString(THEME, value).apply()
    }

    fun getLanguage(context: Context): String {
        return preferences(context).getString(LANGUAGE, "bn") ?: "bn"
    }

    fun saveLanguage(context: Context, value: String) {
        preferences(context).edit().putString(LANGUAGE, value).apply()
    }
}

@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedTheme by remember {
        mutableStateOf(TithiSettingsStorage.getTheme(context))
    }

    var selectedLanguage by remember {
        mutableStateOf(TithiSettingsStorage.getLanguage(context))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SettingsCream)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "সেটিংস",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = SettingsNavy
        )

        Text(
            text = "TITHI আপনার পছন্দ অনুযায়ী সাজিয়ে নিন",
            fontSize = 14.sp,
            color = SettingsMuted
        )

        // Appearance
        SettingsSection(title = "Appearance") {

            Text(
                text = "অ্যাপের থিম",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SettingsText
            )

            SettingsOption(
                title = "System Default",
                description = "ফোনের থিম অনুসরণ করবে",
                selected = selectedTheme == "system"
            ) {
                selectedTheme = "system"
                TithiSettingsStorage.saveTheme(context, "system")
            }

            SettingsOption(
                title = "Light Mode",
                description = "সাদা ও হালকা রঙের থিম",
                selected = selectedTheme == "light"
            ) {
                selectedTheme = "light"
                TithiSettingsStorage.saveTheme(context, "light")
            }

            SettingsOption(
                title = "Dark Mode",
                description = "কালো ও গাঢ় রঙের থিম",
                selected = selectedTheme == "dark"
            ) {
                selectedTheme = "dark"
                TithiSettingsStorage.saveTheme(context, "dark")
            }
        }

        // Language
        SettingsSection(title = "Language") {

            Text(
                text = "অ্যাপের ভাষা",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SettingsText
            )

            SettingsOption(
                title = "বাংলা",
                description = "বাংলা ভাষায় অ্যাপ ব্যবহার করুন",
                selected = selectedLanguage == "bn"
            ) {
                selectedLanguage = "bn"
                TithiSettingsStorage.saveLanguage(context, "bn")
            }

            SettingsOption(
                title = "English",
                description = "Use the app in English",
                selected = selectedLanguage == "en"
            ) {
                selectedLanguage = "en"
                TithiSettingsStorage.saveLanguage(context, "en")
            }
        }

        // About
        SettingsSection(title = "About TITHI") {

            AboutRow("App Name", "TITHI")
            AboutRow("Version", "1.0")
            AboutRow("Developer", "Unlogistix CH")

            HorizontalDivider(
                color = SettingsBorder,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "একটি সুন্দর, সহজ ও নির্ভরযোগ্য সনাতন ধর্মীয় পঞ্জিকা অ্যাপ।",
                fontSize = 13.sp,
                color = SettingsMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SettingsWhite
        ),
        border = BorderStroke(1.dp, SettingsBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SettingsNavy
                )

                content()
            }
        )
    }
}

@Composable
private fun SettingsOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SettingsText
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = SettingsMuted
            )
        }

        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = SettingsNavy
            )
        )
    }
}

@Composable
private fun AboutRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            color = SettingsMuted
        )

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = SettingsText
        )
    }
}


package com.unlogistix.tithi

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

// ─── TITHI PREMIUM PALETTE ─────────────────────────────

private val MyCream = Color(0xFFF7F5EF)
private val MyWhite = Color(0xFFFFFEFB)
private val MyNavy = Color(0xFF233F5B)
private val MyDeepNavy = Color(0xFF1C344C)
private val MyGold = Color(0xFFB18A50)
private val MyGoldSoft = Color(0xFFF3EBDD)
private val MyText = Color(0xFF292F35)
private val MyMuted = Color(0xFF858A8F)
private val MyLine = Color(0xFFEAE6DD)
private val MyBlue = Color(0xFFE9EFF4)
private val MyRed = Color(0xFFB44B45)
private val MyGreen = Color(0xFF547D67)
private val myTimeZone = TimeZone.getTimeZone("Asia/Dhaka")

private fun myToday(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US)
        .apply { timeZone = myTimeZone }
        .format(Date())

private fun myFormatDate(date: Date): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US)
        .apply { timeZone = myTimeZone }
        .format(date)

private fun shiftMyDate(date: String, amount: Int): String {
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    parser.isLenient = false
    parser.timeZone = myTimeZone
    val calendar = Calendar.getInstance(myTimeZone)
    calendar.time = parser.parse(date) ?: Date()
    calendar.add(Calendar.DAY_OF_MONTH, amount)
    return myFormatDate(calendar.time)
}

private fun formatMyTime(time: String): String {
    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = if (hour % 12 == 0) 12 else hour % 12
    return String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm)
}

private fun formatMyDisplayDate(date: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        parser.timeZone = myTimeZone
        val parsed = parser.parse(date) ?: return date
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(parsed)
    } catch (_: Exception) {
        date
    }
}

private fun myTodayLabel(): String {
    return SimpleDateFormat(
        "EEEE, dd MMMM yyyy",
        Locale("bn", "BD")
    ).apply {
        timeZone = myTimeZone
    }.format(Date())
}

// ─── DATA MODELS ───────────────────────────────────────

data class MyDayTask(
    val id: String,
    val title: String,
    val note: String = "",
    val dueDate: String = myToday(),
    val dueTime: String = "09:00",
    val priority: String = "সাধারণ",
    val completed: Boolean = false,
    val completedDate: String = "",
    val reminder: Boolean = false
)

data class MyDayHabit(
    val id: String,
    val title: String,
    val completedDates: List<String> = emptyList()
)

// ─── LOCAL STORAGE ─────────────────────────────────────

internal object MyDayStorage {

    private const val PREFS = "tithi_my_day"
    private const val TASKS = "tasks"
    private const val HABITS = "habits"

    fun loadTasks(context: android.content.Context): List<MyDayTask> {
        val prefs = context.getSharedPreferences(PREFS, 0)
        val array = try {
            JSONArray(prefs.getString(TASKS, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                add(
                    MyDayTask(
                        id = item.optString("id", UUID.randomUUID().toString()),
                        title = item.optString("title"),
                        note = item.optString("note"),
                        dueDate = item.optString("dueDate", myToday()),
                        dueTime = item.optString("dueTime", "09:00"),
                        priority = item.optString("priority", "সাধারণ"),
                        completed = item.optBoolean("completed", false),
                        completedDate = item.optString("completedDate", ""),
                        reminder = item.optBoolean("reminder", false)
                    )
                )
            }
        }
    }

    fun saveTasks(context: android.content.Context, tasks: List<MyDayTask>) {
        val array = JSONArray()
        tasks.forEach { task ->
            array.put(
                JSONObject().apply {
                    put("id", task.id)
                    put("title", task.title)
                    put("note", task.note)
                    put("dueDate", task.dueDate)
                    put("dueTime", task.dueTime)
                    put("priority", task.priority)
                    put("completed", task.completed)
                    put("completedDate", task.completedDate)
                    put("reminder", task.reminder)
                }
            )
        }
        context.getSharedPreferences(PREFS, 0)
            .edit().putString(TASKS, array.toString()).apply()
    }

    fun loadHabits(context: android.content.Context): List<MyDayHabit> {
        val prefs = context.getSharedPreferences(PREFS, 0)
        val array = try {
            JSONArray(prefs.getString(HABITS, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val dates = mutableListOf<String>()
                val history = item.optJSONArray("completedDates")

                if (history != null) {
                    for (j in 0 until history.length()) {
                        val date = history.optString(j)
                        if (date.isNotBlank()) dates.add(date)
                    }
                } else {
                    val oldDate = item.optString("lastCompletedDate", "")
                    if (oldDate.isNotBlank()) dates.add(oldDate)
                }

                add(
                    MyDayHabit(
                        id = item.optString("id", UUID.randomUUID().toString()),
                        title = item.optString("title"),
                        completedDates = dates.distinct().sorted()
                    )
                )
            }
        }
    }

    fun saveHabits(context: android.content.Context, habits: List<MyDayHabit>) {
        val array = JSONArray()
        habits.forEach { habit ->
            val history = JSONArray()
            habit.completedDates.distinct().sorted().forEach { history.put(it) }

            array.put(
                JSONObject().apply {
                    put("id", habit.id)
                    put("title", habit.title)
                    put("completedDates", history)
                    put("lastCompletedDate", habit.completedDates.maxOrNull() ?: "")
                }
            )
        }
        context.getSharedPreferences(PREFS, 0)
            .edit().putString(HABITS, array.toString()).apply()
    }
}

// ─── MAIN SCREEN ───────────────────────────────────────

@Composable
fun MyDayScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val tasks = remember {
        mutableStateListOf<MyDayTask>().apply {
            addAll(MyDayStorage.loadTasks(context))
        }
    }

    val habits = remember {
        mutableStateListOf<MyDayHabit>().apply {
            addAll(MyDayStorage.loadHabits(context))
        }
    }

    var selectedTab by remember { mutableStateOf("আজ") }
    var showTaskEditor by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<MyDayTask?>(null) }
    var showHabitEditor by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<MyDayHabit?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(tasks.toList()) {
        MyDayStorage.saveTasks(context, tasks.toList())
    }

    LaunchedEffect(habits.toList()) {
        MyDayStorage.saveHabits(context, habits.toList())
    }

    val today = myToday()
    val todayTasks = tasks.filter { it.dueDate == today }
    val completedToday = todayTasks.count { it.completed }
    val progress = if (todayTasks.isEmpty()) 0f
    else completedToday.toFloat() / todayTasks.size.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MyCream)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(18.dp))

        // Premium heading
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "T I T H I",
                    color = MyGold,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "আমার দিন",
                    color = MyDeepNavy,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "পরিকল্পনা • অভ্যাস • অগ্রগতি",
                    color = MyMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MyGoldSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "ত",
                    color = MyNavy,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        Spacer(Modifier.height(17.dp))

        // Premium progress panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(23.dp),
            colors = CardDefaults.cardColors(containerColor = MyDeepNavy)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(19.dp)
            ) {
                Text(
                    "আজকের অগ্রগতি",
                    color = Color(0xFFE8D8B9),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(Modifier.height(7.dp))

                Text(
                    myTodayLabel(),
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(19.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "$completedToday / ${todayTasks.size}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            "টি কাজ সম্পন্ন",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(65.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${(progress * 100).toInt()}%",
                            color = Color(0xFFE8D8B9),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Spacer(Modifier.height(17.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(CircleShape),
                    color = Color(0xFFD7B77E),
                    trackColor = Color.White.copy(alpha = 0.16f)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    when {
                        todayTasks.isEmpty() -> "আজকের পরিকল্পনা শুরু করো"
                        progress == 1f -> "আজকের সব কাজ সম্পন্ন হয়েছে!"
                        else -> "ধীরে ধীরে এগিয়ে চলো"
                    },
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(19.dp))

        // Minimal segmented navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFFECE9E1))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            listOf("আজ", "সব", "সম্পন্ন", "রিপোর্ট").forEach { tab ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedTab == tab) MyWhite
                            else Color.Transparent
                        )
                        .clickable { selectedTab = tab }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        tab,
                        color = if (selectedTab == tab) MyNavy else MyMuted,
                        fontWeight = if (selectedTab == tab)
                            FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        if (selectedTab == "রিপোর্ট") {
            MyDayReport(
                tasks = tasks.toList(),
                habits = habits.toList(),
                modifier = Modifier.weight(1f)
            )
        } else {
            val visibleTasks = when (selectedTab) {
                "আজ" -> tasks.filter { it.dueDate == today }
                "সম্পন্ন" -> tasks.filter { it.completed }
                else -> tasks.toList()
            }.sortedWith(
                compareBy<MyDayTask> { it.completed }
                    .thenBy { it.dueDate }
                    .thenBy { it.dueTime }
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    SectionHeader(
                        title = "কাজের তালিকা",
                        subtitle = "${visibleTasks.size} টি কাজ",
                        action = "কাজ যোগ করুন",
                        onAction = {
                            editingTask = null
                            showTaskEditor = true
                        }
                    )
                }

                if (visibleTasks.isEmpty()) {
                    item {
                        EmptyMyDayCard(
                            "এখানে এখনো কোনো কাজ নেই।",
                            "নতুন কাজ যোগ করে তোমার দিনটি সাজিয়ে নাও।"
                        )
                    }
                }

                items(visibleTasks, key = { it.id }) { task ->
                    MyDayTaskCard(
                        task = task,
                        onToggle = { checked ->
                            val index = tasks.indexOfFirst { it.id == task.id }
                            if (index >= 0) {
                                val updated = task.copy(
                                    completed = checked,
                                    completedDate = if (checked) today else ""
                                )
                                tasks[index] = updated

                                if (checked) {
                                    ReminderScheduler.cancel(context, task.id)
                                } else if (updated.reminder) {
                                    ReminderScheduler.schedule(context, updated)
                                }
                            }
                        },
                        onEdit = {
                            editingTask = task
                            showTaskEditor = true
                        },
                        onDelete = {
                            ReminderScheduler.cancel(context, task.id)
                            tasks.removeAll { it.id == task.id }
                        }
                    )
                }

                if (selectedTab != "সম্পন্ন") {
                    item {
                        Spacer(Modifier.height(8.dp))
                        SectionHeader(
                            title = "দৈনিক অভ্যাস",
                            subtitle = "${habits.size} টি অভ্যাস",
                            action = "অভ্যাস যোগ করুন",
                            onAction = {
                                editingHabit = null
                                showHabitEditor = true
                            }
                        )
                    }

                    if (habits.isEmpty()) {
                        item {
                            EmptyMyDayCard(
                                "এখনো কোনো অভ্যাস যোগ করা হয়নি।",
                                "প্রতিদিনের ভালো অভ্যাস গড়ে তুলতে এখানে যোগ করো।"
                            )
                        }
                    }

                    items(habits.toList(), key = { it.id }) { habit ->
                        MyDayHabitCard(
                            habit = habit,
                            today = today,
                            onToggle = { checked ->
                                val index = habits.indexOfFirst { it.id == habit.id }
                                if (index >= 0) {
                                    val dates = habit.completedDates.toMutableSet()
                                    if (checked) dates.add(today)
                                    else dates.remove(today)
                                    habits[index] = habit.copy(
                                        completedDates = dates.sorted()
                                    )
                                }
                            },
                            onEdit = {
                                editingHabit = habit
                                showHabitEditor = true
                            },
                            onDelete = {
                                habits.removeAll { it.id == habit.id }
                            }
                        )
                    }
                }

                item { Spacer(Modifier.height(22.dp)) }
            }
        }
    }

    // Task dialog
    if (showTaskEditor) {
        TaskEditorDialog(
            initial = editingTask,
            onDismiss = { showTaskEditor = false },
            onSave = { task ->
                val index = tasks.indexOfFirst { it.id == task.id }
                if (index >= 0) tasks[index] = task
                else tasks.add(task)

                ReminderScheduler.cancel(context, task.id)

                if (task.reminder && !task.completed) {
                    if (
                        Build.VERSION.SDK_INT >= 33 &&
                        context.checkSelfPermission(
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(
                            android.Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                    ReminderScheduler.schedule(context, task)
                }
                showTaskEditor = false
            }
        )
    }

    // Habit dialog
    if (showHabitEditor) {
        HabitEditorDialog(
            initial = editingHabit,
            onDismiss = { showHabitEditor = false },
            onSave = { habit ->
                val index = habits.indexOfFirst { it.id == habit.id }
                if (index >= 0) habits[index] = habit
                else habits.add(habit)
                showHabitEditor = false
            }
        )
    }
}

// ─── SECTION HEADER ────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    action: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = MyDeepNavy,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                color = MyMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }

        TextButton(
            onClick = onAction,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Text(
                "+ $action",
                color = MyNavy,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─── TASK CARD ─────────────────────────────────────────

@Composable
private fun MyDayTaskCard(
    task: MyDayTask,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val priorityColor = when (task.priority) {
        "জরুরি" -> MyRed
        "গুরুত্বপূর্ণ" -> MyGold
        else -> MyNavy
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = MyWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, MyLine),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 9.dp, end = 9.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = MyNavy,
                    uncheckedColor = MyMuted
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Text(
                    task.title,
                    color = if (task.completed) MyMuted else MyText,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )

                if (task.note.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        task.note,
                        color = MyMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(7.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        formatMyDisplayDate(task.dueDate),
                        color = MyMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text("·", color = MyGold)
                    Text(
                        formatMyTime(task.dueTime),
                        color = MyNavy,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(Modifier.height(7.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                    )
                    Text(
                        task.priority,
                        color = priorityColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (task.reminder) {
                        Text(
                            "· Reminder চালু",
                            color = MyMuted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Box {
                Text(
                    "⋮",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { menuExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    color = MyNavy,
                    style = MaterialTheme.typography.titleLarge
                )

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("সম্পাদনা") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("মুছে ফেলুন", color = MyRed) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

// ─── HABIT CARD ────────────────────────────────────────

@Composable
private fun MyDayHabitCard(
    habit: MyDayHabit,
    today: String,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val done = today in habit.completedDates
    val streak = calculateHabitStreak(habit.completedDates, today)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = MyWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, MyLine),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 9.dp, end = 9.dp, top = 11.dp, bottom = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = done,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = MyGreen,
                    uncheckedColor = MyMuted
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Text(
                    habit.title,
                    color = MyText,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "ধারাবাহিকতা: $streak দিন  ·  মোট: ${habit.completedDates.size} দিন",
                    color = MyMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Box {
                Text(
                    "⋮",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { menuExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    color = MyNavy,
                    style = MaterialTheme.typography.titleLarge
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("সম্পাদনা") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("মুছে ফেলুন", color = MyRed) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

private fun calculateHabitStreak(dates: List<String>, today: String): Int {
    val set = dates.toSet()
    var checkDate = if (today in set) today else shiftMyDate(today, -1)
    var count = 0

    while (checkDate in set && count < 5000) {
        count++
        checkDate = shiftMyDate(checkDate, -1)
    }
    return count
}

// ─── EMPTY STATE ───────────────────────────────────────

@Composable
private fun EmptyMyDayCard(message: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = MyWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, MyLine),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Box(
                Modifier
                    .width(32.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(MyGold)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                message,
                color = MyText,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(5.dp))
            Text(
                subtitle,
                color = MyMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ─── TASK EDITOR ───────────────────────────────────────

@Composable
private fun TaskEditorDialog(
    initial: MyDayTask?,
    onDismiss: () -> Unit,
    onSave: (MyDayTask) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var date by remember { mutableStateOf(initial?.dueDate ?: myToday()) }
    var time by remember { mutableStateOf(initial?.dueTime ?: "09:00") }
    var priority by remember { mutableStateOf(initial?.priority ?: "সাধারণ") }
    var reminder by remember { mutableStateOf(initial?.reminder ?: false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MyCream,
        title = {
            Column {
                Text(
                    if (initial == null) "নতুন কাজ" else "কাজ সম্পাদনা",
                    color = MyNavy,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "তোমার কাজের বিবরণ ঠিক করো",
                    color = MyMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        error = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("কাজের নাম") },
                    singleLine = true,
                    shape = RoundedCornerShape(13.dp)
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("বিস্তারিত (ঐচ্ছিক)") },
                    minLines = 2,
                    shape = RoundedCornerShape(13.dp)
                )

                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance(myTimeZone)
                        val parts = date.split("-")
                        if (parts.size == 3) {
                            calendar.set(
                                parts[0].toIntOrNull() ?: calendar.get(Calendar.YEAR),
                                (parts[1].toIntOrNull() ?: 1) - 1,
                                parts[2].toIntOrNull() ?: 1
                            )
                        }

                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                date = String.format(
                                    Locale.US, "%04d-%02d-%02d",
                                    year, month + 1, day
                                )
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text("তারিখ: ${formatMyDisplayDate(date)}")
                }

                OutlinedButton(
                    onClick = {
                        val parts = time.split(":")
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                time = String.format(
                                    Locale.US, "%02d:%02d", hour, minute
                                )
                            },
                            parts.getOrNull(0)?.toIntOrNull() ?: 9,
                            parts.getOrNull(1)?.toIntOrNull() ?: 0,
                            false
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text(
                        "সময়: ${formatMyTime(time)}",
                        color = MyNavy,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box {
                    OutlinedButton(
                        onClick = { priorityExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(13.dp)
                    ) {
                        Text("অগ্রাধিকার: $priority")
                    }

                    DropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        listOf("সাধারণ", "গুরুত্বপূর্ণ", "জরুরি").forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    priority = it
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MyWhite),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reminder = !reminder }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = reminder,
                            onCheckedChange = { reminder = it },
                            colors = CheckboxDefaults.colors(checkedColor = MyNavy)
                        )
                        Column {
                            Text("Reminder চালু রাখুন", color = MyText)
                            Text(
                                "নির্ধারিত সময়ে বিজ্ঞপ্তি পাবেন",
                                color = MyMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        error = "কাজের নাম লিখতে হবে।"
                    } else {
                        onSave(
                            MyDayTask(
                                id = initial?.id ?: UUID.randomUUID().toString(),
                                title = title.trim(),
                                note = note.trim(),
                                dueDate = date,
                                dueTime = time,
                                priority = priority,
                                completed = initial?.completed ?: false,
                                completedDate = initial?.completedDate ?: "",
                                reminder = reminder
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MyNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("সংরক্ষণ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = MyMuted)
            }
        }
    )
}

// ─── HABIT EDITOR ──────────────────────────────────────

@Composable
private fun HabitEditorDialog(
    initial: MyDayHabit?,
    onDismiss: () -> Unit,
    onSave: (MyDayHabit) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MyCream,
        title = {
            Text(
                if (initial == null) "নতুন অভ্যাস" else "অভ্যাস সম্পাদনা",
                color = MyNavy,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("অভ্যাসের নাম") },
                singleLine = true,
                shape = RoundedCornerShape(13.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            MyDayHabit(
                                id = initial?.id ?: UUID.randomUUID().toString(),
                                title = title.trim(),
                                completedDates = initial?.completedDates ?: emptyList()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MyNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("সংরক্ষণ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = MyMuted)
            }
        }
    )
}

// ─── REPORT ────────────────────────────────────────────

@Composable
private fun MyDayReport(
    tasks: List<MyDayTask>,
    habits: List<MyDayHabit>,
    modifier: Modifier = Modifier
) {
    var period by remember { mutableStateOf("সাপ্তাহিক") }
    val today = myToday()

    val start = if (period == "সাপ্তাহিক") {
        val calendar = Calendar.getInstance(myTimeZone)
        calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .apply { timeZone = myTimeZone }
            .parse(today) ?: Date()

        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val difference = if (day == Calendar.SUNDAY) 6 else day - Calendar.MONDAY
        calendar.add(Calendar.DAY_OF_MONTH, -difference)
        myFormatDate(calendar.time)
    } else {
        today.substring(0, 8) + "01"
    }

    val end = if (period == "সাপ্তাহিক") {
        shiftMyDate(start, 6)
    } else {
        val calendar = Calendar.getInstance(myTimeZone)
        calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .apply { timeZone = myTimeZone }
            .parse(start) ?: Date()
        calendar.set(
            Calendar.DAY_OF_MONTH,
            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        )
        myFormatDate(calendar.time)
    }

    val periodTasks = tasks.filter {
        it.dueDate >= start && it.dueDate <= end
    }
    val completed = periodTasks.count { it.completed }
    val pending = periodTasks.size - completed
    val percent = if (periodTasks.isEmpty()) 0
    else completed * 100 / periodTasks.size

    val habitCompletions = habits.sumOf { habit ->
        habit.completedDates.count { it >= start && it <= end }
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MyWhite)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf("সাপ্তাহিক", "মাসিক").forEach { option ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                if (period == option) MyNavy
                                else Color.Transparent
                            )
                            .clickable { period = option }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            option,
                            color = if (period == option) Color.White else MyMuted,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        item {
            Text(
                "$start  —  $end",
                color = MyMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }

        item {
            ReportNumberCard("মোট কাজ", periodTasks.size.toString())
        }
        item {
            ReportNumberCard("সম্পন্ন কাজ", completed.toString())
        }
        item {
            ReportNumberCard("বাকি কাজ", pending.toString())
        }
        item {
            ReportNumberCard("কাজ সম্পন্নের হার", "$percent%")
        }
        item {
            ReportNumberCard("অভ্যাস সম্পন্ন", habitCompletions.toString())
        }

        item {
            Spacer(Modifier.height(5.dp))
            Text(
                "অভ্যাসের ধারাবাহিকতা",
                color = MyDeepNavy,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (habits.isEmpty()) {
            item {
                EmptyMyDayCard(
                    "এখনো কোনো অভ্যাস নেই।",
                    "অভ্যাস যোগ করলে তার অগ্রগতি এখানে দেখা যাবে।"
                )
            }
        }

        items(habits, key = { it.id }) { habit ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MyWhite),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MyLine),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Text(
                        habit.title,
                        color = MyText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "এই সময়ে ${habit.completedDates.count { it >= start && it <= end }} দিন সম্পন্ন",
                        color = MyMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Text(
                "নোট: পুরোনো সম্পন্ন কাজের তারিখ সংরক্ষিত না থাকলে সেগুলো এই রিপোর্টে গণনা করা হবে না।",
                color = MyMuted,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ReportNumberCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MyWhite),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MyLine),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = MyMuted)
            Text(
                value,
                color = MyNavy,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

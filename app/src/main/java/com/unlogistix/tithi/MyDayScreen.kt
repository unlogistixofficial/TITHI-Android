
package com.unlogistix.tithi

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private val MyCream = Color(0xFFF8F6F0)
private val MyWhite = Color.White
private val MyNavy = Color(0xFF244B70)
private val MyText = Color(0xFF282D32)
private val MyMuted = Color(0xFF777D83)
private val MyBlue = Color(0xFFEAF0F6)

private val myTimeZone = TimeZone.getTimeZone("Asia/Dhaka")

private fun myToday(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US)
        .apply { timeZone = myTimeZone }
        .format(Date())
}

private fun myFormatDate(date: Date): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US)
        .apply { timeZone = myTimeZone }
        .format(date)
}

private fun shiftMyDate(date: String, amount: Int): String {
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    parser.timeZone = myTimeZone
    val calendar = Calendar.getInstance(myTimeZone)
    calendar.time = parser.parse(date) ?: Date()
    calendar.add(Calendar.DAY_OF_MONTH, amount)
    return myFormatDate(calendar.time)
}

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

    fun saveTasks(
        context: android.content.Context,
        tasks: List<MyDayTask>
    ) {
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
            .edit()
            .putString(TASKS, array.toString())
            .apply()
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

    fun saveHabits(
        context: android.content.Context,
        habits: List<MyDayHabit>
    ) {
        val array = JSONArray()

        habits.forEach { habit ->
            val history = JSONArray()

            habit.completedDates.distinct().sorted().forEach {
                history.put(it)
            }

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
            .edit()
            .putString(HABITS, array.toString())
            .apply()
    }
}

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

    val progress = if (todayTasks.isEmpty()) {
        0f
    } else {
        completedToday.toFloat() / todayTasks.size.toFloat()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MyCream)
            .padding(horizontal = 18.dp)
    ) {

        Spacer(Modifier.height(20.dp))

        Text("TITHI", color = MyMuted)
        Text(
            "আমার দিন",
            color = MyNavy,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "পরিকল্পনা, অভ্যাস ও প্রতিদিনের অগ্রগতি",
            color = MyMuted,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MyWhite),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "আজকের অগ্রগতি",
                        color = MyNavy,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "${(progress * 100).toInt()}%",
                        color = MyNavy,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MyNavy,
                    trackColor = MyBlue
                )

                Spacer(Modifier.height(7.dp))

                Text(
                    "$completedToday / ${todayTasks.size} টি কাজ সম্পন্ন",
                    color = MyMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf("আজ", "সব", "সম্পন্ন", "রিপোর্ট").forEach { tab ->
                FilterChip(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    label = { Text(tab) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (selectedTab == "রিপোর্ট") {

            MyDayReport(
                tasks = tasks.toList(),
                habits = habits.toList()
            )

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "কাজের তালিকা",
                            color = MyText,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = {
                                editingTask = null
                                showTaskEditor = true
                            }
                        ) {
                            Text("+ কাজ যোগ", color = MyNavy)
                        }
                    }
                }

                val visibleTasks = when (selectedTab) {
                    "আজ" -> tasks.filter { it.dueDate == today }
                    "সম্পন্ন" -> tasks.filter { it.completed }
                    else -> tasks.toList()
                }.sortedWith(
                    compareBy<MyDayTask> { it.completed }
                        .thenBy { it.dueDate }
                        .thenBy { it.dueTime }
                )

                if (visibleTasks.isEmpty()) {
                    item {
                        EmptyMyDayCard("এখানে এখনো কোনো কাজ নেই।")
                    }
                }

                items(
                    items = visibleTasks,
                    key = { it.id }
                ) { task ->

                    MyDayTaskCard(
                        task = task,

                        onToggle = { checked ->

                            val index = tasks.indexOfFirst {
                                it.id == task.id
                            }

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

                item {
                    Spacer(Modifier.height(12.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "দৈনিক অভ্যাস",
                            color = MyText,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = {
                                editingHabit = null
                                showHabitEditor = true
                            }
                        ) {
                            Text("+ অভ্যাস যোগ", color = MyNavy)
                        }
                    }
                }

                if (habits.isEmpty()) {
                    item {
                        EmptyMyDayCard(
                            "নতুন অভ্যাস যোগ করে প্রতিদিনের অগ্রগতি রাখো।"
                        )
                    }
                }

                items(
                    items = habits.toList(),
                    key = { it.id }
                ) { habit ->

                    MyDayHabitCard(
                        habit = habit,
                        today = today,

                        onToggle = { checked ->

                            val index = habits.indexOfFirst {
                                it.id == habit.id
                            }

                            if (index >= 0) {
                                val dates = habit.completedDates.toMutableSet()

                                if (checked) {
                                    dates.add(today)
                                } else {
                                    dates.remove(today)
                                }

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

                item {
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showTaskEditor) {

        TaskEditorDialog(
            initial = editingTask,

            onDismiss = {
                showTaskEditor = false
            },

            onSave = { task ->

                val index = tasks.indexOfFirst {
                    it.id == task.id
                }

                if (index >= 0) {
                    tasks[index] = task
                } else {
                    tasks.add(task)
                }

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

    if (showHabitEditor) {

        HabitEditorDialog(
            initial = editingHabit,

            onDismiss = {
                showHabitEditor = false
            },

            onSave = { habit ->

                val index = habits.indexOfFirst {
                    it.id == habit.id
                }

                if (index >= 0) {
                    habits[index] = habit
                } else {
                    habits.add(habit)
                }

                showHabitEditor = false
            }
        )
    }
}

@Composable
private fun MyDayTaskCard(
    task: MyDayTask,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MyWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = task.completed,
                onCheckedChange = onToggle
            )

            Column(Modifier.weight(1f)) {

                Text(
                    task.title,
                    color = MyText,
                    fontWeight = FontWeight.SemiBold
                )

                if (task.note.isNotBlank()) {
                    Text(
                        task.note,
                        color = MyMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    "${task.dueDate} • ${task.dueTime} • ${task.priority}" +
                        if (task.reminder) " • Reminder" else "",
                    color = MyMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Column {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }

                TextButton(onClick = onDelete) {
                    Text(
                        "Delete",
                        color = Color(0xFFB44B45)
                    )
                }
            }
        }
    }
}

@Composable
private fun MyDayHabitCard(
    habit: MyDayHabit,
    today: String,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val done = today in habit.completedDates
    val streak = calculateHabitStreak(habit.completedDates, today)

    Card(
        colors = CardDefaults.cardColors(containerColor = MyWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = done,
                onCheckedChange = onToggle
            )

            Column(Modifier.weight(1f)) {

                Text(
                    habit.title,
                    color = MyText,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    "ধারাবাহিকতা: $streak দিন • মোট: ${habit.completedDates.size} দিন",
                    color = MyMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }

                TextButton(onClick = onDelete) {
                    Text("Delete", color = Color(0xFFB44B45))
                }
            }
        }
    }
}

private fun calculateHabitStreak(
    dates: List<String>,
    today: String
): Int {
    val set = dates.toSet()
    var checkDate = if (today in set) today else shiftMyDate(today, -1)
    var count = 0

    while (checkDate in set && count < 5000) {
        count++
        checkDate = shiftMyDate(checkDate, -1)
    }

    return count
}

@Composable
private fun EmptyMyDayCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MyWhite),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            message,
            modifier = Modifier.padding(18.dp),
            color = MyMuted
        )
    }
}

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
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(if (initial == null) "নতুন কাজ" else "কাজ সম্পাদনা")
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        error = ""
                    },
                    label = { Text("কাজের নাম") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("বিস্তারিত (ঐচ্ছিক)") }
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
                                    Locale.US,
                                    "%04d-%02d-%02d",
                                    year,
                                    month + 1,
                                    day
                                )
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("তারিখ: $date")
                }

                OutlinedButton(
                    onClick = {

                        val parts = time.split(":")

                        TimePickerDialog(
                            context,
                            { _, hour, minute ->

                                time = String.format(
                                    Locale.US,
                                    "%02d:%02d",
                                    hour,
                                    minute
                                )
                            },
                            parts.getOrNull(0)?.toIntOrNull() ?: 9,
                            parts.getOrNull(1)?.toIntOrNull() ?: 0,
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("সময়: $time")
                }

                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("অগ্রাধিকার: $priority")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("সাধারণ", "গুরুত্বপূর্ণ", "জরুরি").forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    priority = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = reminder,
                        onCheckedChange = { reminder = it }
                    )

                    Text("এই কাজের জন্য Reminder")
                }

                if (error.isNotBlank()) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },

        confirmButton = {
            TextButton(
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
                }
            ) {
                Text("সংরক্ষণ")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
private fun HabitEditorDialog(
    initial: MyDayHabit?,
    onDismiss: () -> Unit,
    onSave: (MyDayHabit) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(if (initial == null) "নতুন অভ্যাস" else "অভ্যাস সম্পাদনা")
        },

        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("অভ্যাসের নাম") },
                singleLine = true
            )
        },

        confirmButton = {
            TextButton(
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
                }
            ) {
                Text("সংরক্ষণ")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
private fun MyDayReport(
    tasks: List<MyDayTask>,
    habits: List<MyDayHabit>
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

    val percent = if (periodTasks.isEmpty()) {
        0
    } else {
        completed * 100 / periodTasks.size
    }

    val habitCompletions = habits.sumOf { habit ->
        habit.completedDates.count { it >= start && it <= end }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            FilterChip(
                selected = period == "সাপ্তাহিক",
                onClick = { period = "সাপ্তাহিক" },
                label = { Text("সাপ্তাহিক") }
            )

            FilterChip(
                selected = period == "মাসিক",
                onClick = { period = "মাসিক" },
                label = { Text("মাসিক") }
            )
        }

        Text("$start থেকে $end", color = MyMuted)

        ReportNumberCard("মোট কাজ", periodTasks.size.toString())
        ReportNumberCard("সম্পন্ন কাজ", completed.toString())
        ReportNumberCard("বাকি কাজ", pending.toString())
        ReportNumberCard("কাজ সম্পন্নের হার", "$percent%")
        ReportNumberCard("অভ্যাস সম্পন্ন", habitCompletions.toString())

        Text(
            "অভ্যাসের ধারাবাহিকতা",
            color = MyNavy,
            fontWeight = FontWeight.Bold
        )

        habits.forEach { habit ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MyWhite)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {

                    Text(
                        habit.title,
                        color = MyText,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "এই সময়ে ${habit.completedDates.count { it >= start && it <= end }} দিন সম্পন্ন",
                        color = MyMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Text(
            "নোট: পুরোনো সম্পন্ন কাজের তারিখ সংরক্ষিত না থাকলে সেগুলো এই রিপোর্টে গণনা করা হবে না।",
            color = MyMuted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ReportNumberCard(
    label: String,
    value: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MyWhite),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(label, color = MyMuted)

            Text(
                value,
                color = MyNavy,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

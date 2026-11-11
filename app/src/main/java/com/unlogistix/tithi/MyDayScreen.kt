
package com.unlogistix.tithi

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val MyDayCream = Color(0xFFF8F6F0)
private val MyDayWhite = Color(0xFFFFFFFF)
private val MyDayNavy = Color(0xFF244B70)
private val MyDayText = Color(0xFF282D32)
private val MyDayMuted = Color(0xFF777D83)
private val MyDayBlue = Color(0xFFEAF0F6)
private val MyDayBorder = Color(0xFFE9E5DC)
private val MyDayGreen = Color(0xFF47785B)

private fun todayDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

private data class MyDayTask(
    val id: Long,
    val title: String,
    val note: String,
    val dueDate: String,
    val dueTime: String,
    val priority: String,
    val completed: Boolean
)

private data class MyDayHabit(
    val id: Long,
    val title: String,
    val lastCompletedDate: String
)

private object MyDayStorage {

    private const val PREFS = "tithi_my_day"
    private const val TASKS_KEY = "tasks"
    private const val HABITS_KEY = "habits"

    fun loadTasks(context: Context): List<MyDayTask> {
        return try {
            val raw = context.getSharedPreferences(
                PREFS, Context.MODE_PRIVATE
            ).getString(TASKS_KEY, "[]") ?: "[]"

            val array = JSONArray(raw)
            val result = mutableListOf<MyDayTask>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                result.add(
                    MyDayTask(
                        id = item.optLong("id"),
                        title = item.optString("title"),
                        note = item.optString("note"),
                        dueDate = item.optString("dueDate"),
                        dueTime = item.optString("dueTime"),
                        priority = item.optString("priority", "সাধারণ"),
                        completed = item.optBoolean("completed")
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveTasks(context: Context, tasks: List<MyDayTask>) {
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
                }
            )
        }

        context.getSharedPreferences(
            PREFS, Context.MODE_PRIVATE
        ).edit().putString(TASKS_KEY, array.toString()).apply()
    }

    fun loadHabits(context: Context): List<MyDayHabit> {
        return try {
            val raw = context.getSharedPreferences(
                PREFS, Context.MODE_PRIVATE
            ).getString(HABITS_KEY, "[]") ?: "[]"

            val array = JSONArray(raw)
            val result = mutableListOf<MyDayHabit>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                result.add(
                    MyDayHabit(
                        id = item.optLong("id"),
                        title = item.optString("title"),
                        lastCompletedDate =
                            item.optString("lastCompletedDate")
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveHabits(context: Context, habits: List<MyDayHabit>) {
        val array = JSONArray()

        habits.forEach { habit ->
            array.put(
                JSONObject().apply {
                    put("id", habit.id)
                    put("title", habit.title)
                    put("lastCompletedDate", habit.lastCompletedDate)
                }
            )
        }

        context.getSharedPreferences(
            PREFS, Context.MODE_PRIVATE
        ).edit().putString(HABITS_KEY, array.toString()).apply()
    }
}

@Composable
fun MyDayScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var tasks by remember {
        mutableStateOf(MyDayStorage.loadTasks(context))
    }

    var habits by remember {
        mutableStateOf(MyDayStorage.loadHabits(context))
    }

    var selectedFilter by remember { mutableStateOf("আজকের কাজ") }
    var showTaskDialog by remember { mutableStateOf(false) }
    var showHabitDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<MyDayTask?>(null) }
    var deletingTask by remember { mutableStateOf<MyDayTask?>(null) }

    val today = todayDate()

    val todayTasks = tasks.filter { it.dueDate == today }
    val completedToday = todayTasks.count { it.completed }
    val progress = if (todayTasks.isEmpty()) 0f
        else completedToday.toFloat() / todayTasks.size

    val doneHabits = habits.count {
        it.lastCompletedDate == today
    }

    val visibleTasks = when (selectedFilter) {
        "আজকের কাজ" -> todayTasks
        "সম্পন্ন" -> tasks.filter { it.completed }
        else -> tasks.sortedBy { it.dueDate }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MyDayCream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(22.dp))

        Text(
            "TITHI",
            fontSize = 11.sp,
            color = MyDayMuted,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(7.dp))

        Text(
            "আমার দিন",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MyDayNavy
        )

        Spacer(Modifier.height(5.dp))

        Text(
            "তোমার প্রতিদিনের কাজ ও অভ্যাসের সঙ্গী।",
            fontSize = 13.sp,
            color = MyDayMuted
        )

        Spacer(Modifier.height(22.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MyDayWhite
            ),
            border = BorderStroke(1.dp, MyDayBorder)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "আজকের অগ্রগতি",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MyDayNavy
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "$completedToday / ${todayTasks.size} কাজ সম্পন্ন",
                        fontSize = 13.sp,
                        color = MyDayText
                    )
                    Text(
                        "${(progress * 100).toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MyDayNavy
                    )
                }

                Spacer(Modifier.height(9.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MyDayNavy,
                    trackColor = MyDayBlue
                )

                Spacer(Modifier.height(15.dp))

                HorizontalDivider(color = MyDayBorder)

                Spacer(Modifier.height(13.dp))

                Text(
                    "আজকের অভ্যাস: $doneHabits / ${habits.size} সম্পন্ন",
                    fontSize = 13.sp,
                    color = MyDayMuted
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "আমার কাজ",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyDayText
                )
                Text(
                    today,
                    fontSize = 12.sp,
                    color = MyDayMuted
                )
            }

            Button(
                onClick = {
                    editingTask = null
                    showTaskDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MyDayNavy
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ নতুন কাজ")
            }
        }

        Spacer(Modifier.height(13.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("আজকের কাজ", "সব কাজ", "সম্পন্ন").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (visibleTasks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MyDayWhite
                ),
                border = BorderStroke(1.dp, MyDayBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "এখনো কোনো কাজ যোগ করা হয়নি",
                        fontSize = 14.sp,
                        color = MyDayMuted
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "নতুন কাজ যোগ করে তোমার দিন সাজাও।",
                        fontSize = 12.sp,
                        color = MyDayMuted
                    )
                }
            }
        } else {
            visibleTasks.forEach { task ->
                TaskCard(
                    task = task,
                    onToggle = {
                        val updated = tasks.map {
                            if (it.id == task.id) {
                                it.copy(completed = !it.completed)
                            } else it
                        }
                        tasks = updated
                        MyDayStorage.saveTasks(context, updated)
                    },
                    onEdit = {
                        editingTask = task
                        showTaskDialog = true
                    },
                    onDelete = {
                        deletingTask = task
                    }
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(25.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "আমার অভ্যাস",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyDayText
                )
                Text(
                    "প্রতিদিন নিজেকে একটু উন্নত করো।",
                    fontSize = 12.sp,
                    color = MyDayMuted
                )
            }

            TextButton(onClick = { showHabitDialog = true }) {
                Text("+ অভ্যাস", color = MyDayNavy)
            }
        }

        Spacer(Modifier.height(12.dp))

        if (habits.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MyDayWhite
                ),
                border = BorderStroke(1.dp, MyDayBorder)
            ) {
                Text(
                    "যেমন: প্রতিদিন পড়াশোনা, প্রার্থনা বা ব্যায়াম।",
                    modifier = Modifier.padding(18.dp),
                    fontSize = 13.sp,
                    color = MyDayMuted
                )
            }
        } else {
            habits.forEach { habit ->
                val isDone = habit.lastCompletedDate == today

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MyDayWhite
                    ),
                    border = BorderStroke(1.dp, MyDayBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 13.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = {
                                val updated = habits.map {
                                    if (it.id == habit.id) {
                                        it.copy(
                                            lastCompletedDate =
                                                if (isDone) "" else today
                                        )
                                    } else it
                                }
                                habits = updated
                                MyDayStorage.saveHabits(context, updated)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MyDayGreen
                            )
                        )

                        Text(
                            habit.title,
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            color = MyDayText
                        )

                        TextButton(
                            onClick = {
                                val updated = habits.filterNot {
                                    it.id == habit.id
                                }
                                habits = updated
                                MyDayStorage.saveHabits(context, updated)
                            }
                        ) {
                            Text("মুছুন", color = MyDayMuted)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "TITHI • প্রতিদিনের ছোট ছোট অগ্রগতি",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 11.sp,
            color = MyDayMuted
        )

        Spacer(Modifier.height(22.dp))
    }

    if (showTaskDialog) {
        TaskEditorDialog(
            existing = editingTask,
            onDismiss = { showTaskDialog = false },
            onSave = { title, note, date, time, priority ->
                val old = editingTask
                val newTask = MyDayTask(
                    id = old?.id ?: System.currentTimeMillis(),
                    title = title,
                    note = note,
                    dueDate = date,
                    dueTime = time,
                    priority = priority,
                    completed = old?.completed ?: false
                )

                val updated = if (old == null) {
                    tasks + newTask
                } else {
                    tasks.map {
                        if (it.id == old.id) newTask else it
                    }
                }

                tasks = updated
                MyDayStorage.saveTasks(context, updated)
                showTaskDialog = false
            }
        )
    }

    if (showHabitDialog) {
        HabitEditorDialog(
            onDismiss = { showHabitDialog = false },
            onSave = { title ->
                val updated = habits + MyDayHabit(
                    id = System.currentTimeMillis(),
                    title = title,
                    lastCompletedDate = ""
                )
                habits = updated
                MyDayStorage.saveHabits(context, updated)
                showHabitDialog = false
            }
        )
    }

    deletingTask?.let { task ->
        AlertDialog(
            onDismissRequest = { deletingTask = null },
            title = { Text("কাজ মুছে ফেলবে?") },
            text = { Text("“${task.title}” কাজটি মুছে ফেলা হবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updated = tasks.filterNot {
                            it.id == task.id
                        }
                        tasks = updated
                        MyDayStorage.saveTasks(context, updated)
                        deletingTask = null
                    }
                ) {
                    Text("মুছে ফেলো", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTask = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun TaskCard(
    task: MyDayTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MyDayWhite
        ),
        border = BorderStroke(1.dp, MyDayBorder)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MyDayGreen
                    )
                )

                Column(Modifier.weight(1f)) {
                    Text(
                        task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MyDayText
                    )

                    if (task.note.isNotBlank()) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            task.note,
                            fontSize = 12.sp,
                            color = MyDayMuted
                        )
                    }
                }

                Text(
                    task.priority,
                    fontSize = 11.sp,
                    color = MyDayNavy
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MyDayBorder
            )

            Text(
                "তারিখ: ${task.dueDate}" +
                    if (task.dueTime.isNotBlank()) {
                        "  •  সময়: ${task.dueTime}"
                    } else "",
                fontSize = 11.sp,
                color = MyDayMuted
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("সম্পাদনা")
                }
                TextButton(onClick = onDelete) {
                    Text("মুছুন", color = Color(0xFFB44343))
                }
            }
        }
    }
}

@Composable
private fun TaskEditorDialog(
    existing: MyDayTask?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var title by remember(existing) {
        mutableStateOf(existing?.title ?: "")
    }
    var note by remember(existing) {
        mutableStateOf(existing?.note ?: "")
    }
    var date by remember(existing) {
        mutableStateOf(existing?.dueDate ?: todayDate())
    }
    var time by remember(existing) {
        mutableStateOf(existing?.dueTime ?: "")
    }
    var priority by remember(existing) {
        mutableStateOf(existing?.priority ?: "সাধারণ")
    }
    var priorityMenu by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existing == null) "নতুন কাজ" else "কাজ সম্পাদনা")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("কাজের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("বিস্তারিত (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("তারিখ (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("সময় (HH:MM, ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Box {
                    OutlinedButton(
                        onClick = { priorityMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("অগ্রাধিকার: $priority")
                    }

                    DropdownMenu(
                        expanded = priorityMenu,
                        onDismissRequest = { priorityMenu = false }
                    ) {
                        listOf("জরুরি", "সাধারণ", "কম গুরুত্বপূর্ণ")
                            .forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        priority = option
                                        priorityMenu = false
                                    }
                                )
                            }
                    }
                }

                if (error.isNotBlank()) {
                    Text(
                        error,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val validDate = Regex("\\d{4}-\\d{2}-\\d{2}")
                        .matches(date)
                    val validTime = time.isBlank() ||
                        Regex("\\d{2}:\\d{2}").matches(time)

                    error = when {
                        title.isBlank() -> "কাজের নাম লিখো।"
                        !validDate -> "তারিখ YYYY-MM-DD ফরম্যাটে লিখো।"
                        !validTime -> "সময় HH:MM ফরম্যাটে লিখো।"
                        else -> ""
                    }

                    if (error.isBlank()) {
                        onSave(
                            title.trim(),
                            note.trim(),
                            date,
                            time,
                            priority
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
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন অভ্যাস") },
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
                        onSave(title.trim())
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

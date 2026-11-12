package com.unlogistix.tithi

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal object ReminderScheduler {
    private const val CHANNEL_ID = "tithi_task_reminders"

    fun schedule(context: Context, task: MyDayTask) {
        if (!task.reminder || task.completed) return

        val formatter = SimpleDateFormat(
            "yyyy-MM-dd HH:mm",
            Locale.US
        ).apply {
            timeZone = TimeZone.getTimeZone("Asia/Dhaka")
        }

        val triggerTime = try {
            formatter.parse("${task.dueDate} ${task.dueTime}")?.time
        } catch (_: Exception) {
            null
        } ?: return

        if (triggerTime <= System.currentTimeMillis()) return

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.unlogistix.tithi.TASK_REMINDER"
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("task_note", task.note)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    fun cancel(context: Context, taskId: String) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.unlogistix.tithi.TASK_REMINDER"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun restoreAll(context: Context) {
        MyDayStorage.loadTasks(context).forEach { task ->
            if (task.reminder && !task.completed) {
                schedule(context, task)
            }
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TITHI Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "TITHI app task reminders"
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        taskId: String,
        title: String,
        note: String
    ) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createNotificationChannel(context)

        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)

        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context,
                taskId.hashCode(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(note.ifBlank { "তোমার নির্ধারিত কাজের সময় হয়েছে।" })
                .setStyle(
                    Notification.BigTextStyle().bigText(
                        note.ifBlank { "তোমার নির্ধারিত কাজের সময় হয়েছে।" }
                    )
                )
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(note.ifBlank { "তোমার নির্ধারিত কাজের সময় হয়েছে।" })
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build()
        }

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(taskId.hashCode(), notification)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("task_id") ?: return
        val title = intent.getStringExtra("task_title") ?: "TITHI Reminder"
        val note = intent.getStringExtra("task_note") ?: ""

        ReminderScheduler.showNotification(
            context,
            id,
            title,
            note
        )
    }
}

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            ReminderScheduler.restoreAll(context)
        }
    }
}

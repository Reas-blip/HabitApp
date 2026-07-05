package android.learn.habitapp.notifications

import android.Manifest
import android.R
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.learn.habitapp.MainActivity
import android.learn.habitapp.data.local.FrequencyType
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.repository.HabitRepository
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.LocalTime
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var habitRepository: HabitRepository

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getIntExtra("habitId", -1)
        if (habitId == -1) return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habit = habitRepository.load(habitId)

                if (!habit.isArchived) {
                    if (FrequencyEvaluator.shouldNotifyToday(habit, habitRepository)) {
                        showNotification(context, habit)
                    }
                    habit.reminderTime?.let { alarmScheduler.schedule(habit) }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context, habit: HabitEntity) {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("openHabitId", habit.id)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, habit.id, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "habit_reminders")
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("${habit.emoji} ${habit.name}")
            .setContentText("Don't forget to complete this habit today")
            .setContentIntent(contentPendingIntent) // ← this is what makes tapping it work
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(habit.id, notification)
    }
}


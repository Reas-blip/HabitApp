package android.learn.habitapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.learn.habitapp.data.repository.HabitRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var habitRepository: HabitRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                habitRepository.getHabitsWithLogs().first().forEach { habitWithLogs ->
                    val habit = habitWithLogs.habit
                    val time = habit.reminderTime?.let { LocalTime.parse(it) }
                    if (time != null && !habit.isArchived) {
                        alarmScheduler.schedule(habit)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
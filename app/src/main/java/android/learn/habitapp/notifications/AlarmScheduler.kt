package android.learn.habitapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.learn.habitapp.data.local.HabitEntity
import android.net.Uri
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
   @param:ApplicationContext private val context: Context
) {
   private val alarmManager = context.getSystemService(AlarmManager::class.java)
   fun schedule(habit: HabitEntity) {
      val time = habit.reminderTime?.let { LocalTime.parse(it) } ?: return
      val nextDate = FrequencyEvaluator.nextValidDate(habit)
      val triggerTime = nextDate.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

      val intent = Intent(context, ReminderReceiver::class.java).apply {
         putExtra("habitId", habit.id)
         putExtra("habitName", habit.name)
         putExtra("emoji", habit.emoji)
      }
      val pendingIntent = PendingIntent.getBroadcast(
         context, habit.id, intent,
         PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      if (canScheduleExactAlarms()) {
         alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      } else {
         alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      }
   }

   fun canScheduleExactAlarms(): Boolean {
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
         alarmManager.canScheduleExactAlarms()
      } else {
         true // permission doesn't exist before API 31, exact is always allowed
      }
   }

   fun requestExactAlarmPermission(context: Context) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
         val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
         }
         context.startActivity(intent)
      }
   }
   fun cancel(habitId: Int) {
      val intent = Intent(context, ReminderReceiver::class.java)
      val pendingIntent = PendingIntent.getBroadcast(
         context, habitId, intent,
         PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      alarmManager.cancel(pendingIntent)
   }

}

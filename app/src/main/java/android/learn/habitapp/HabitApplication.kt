package android.learn.habitapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HabitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
           "habit_reminders",
           "Habit Reminders",
           NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders to complete your habits"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
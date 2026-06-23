package android.learn.habitapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity (
   @PrimaryKey(autoGenerate = true)
   val id: Int = 0,
   val name: String,
   val iconName: String,
   val createdAt: Long = System.currentTimeMillis()

)
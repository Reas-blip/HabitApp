package android.learn.habitapp.data.local

import androidx.room.TypeConverter

class HabitConverters {
   @TypeConverter
   fun fromFrequencyType(value: FrequencyType): String = value.name

   @TypeConverter
   fun toFrequencyType(value: String): FrequencyType = FrequencyType.valueOf(value)
}

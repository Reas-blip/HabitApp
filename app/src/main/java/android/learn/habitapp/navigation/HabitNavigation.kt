package android.learn.habitapp.navigation
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination

@Serializable
data object HabitList : Destination

@Serializable
data class HabitDetail(
   val habitId: Int? = null
) : Destination
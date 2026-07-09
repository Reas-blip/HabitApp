package android.learn.habitapp.navigation
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination

@Serializable
data object HabitListScreen : Destination

@Serializable
data object ArchivedHabitScreen: Destination
@Serializable
data class HabitDetail(
   val habitId: Int? = null, val currentToken: Long = 0L) : Destination
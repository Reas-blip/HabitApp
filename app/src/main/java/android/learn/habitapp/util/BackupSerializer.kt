package android.learn.habitapp.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Frequency stored as its enum name (String) rather than the enum type itself,
// so this file never needs to depend on FrequencyType being @Serializable —
// HabitEntity.kt stays untouched.
@Serializable
data class HabitBackupDto(
    val groupId: String,
    val name: String,
    val emoji: String,
    val frequencyType: String,
    val customDays: String? = null,
    val timesPerWeek: Int? = null,
    val reminderTime: String? = null,
    val color: Int? = null,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false,
    val isReplaced: Boolean = false,
    val replacedAt: Long? = null,
    val createdAt: Long,
    val logDates: List<Long> = emptyList()
)

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val exportedAt: Long,
    val habits: List<HabitBackupDto>
)

// ignoreUnknownKeys: forward/backward compatible if this schema grows later —
// an older app importing a newer export (or vice versa) won't crash on unknown fields.
val backupJson: Json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

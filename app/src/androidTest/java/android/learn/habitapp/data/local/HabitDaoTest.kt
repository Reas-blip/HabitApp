package android.learn.habitapp.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * These run against a real, in-memory Room database (real SQLite, real generated SQL,
 * real TypeConverters) rather than a mock repository. That's the point: the earlier
 * HabitRow/ConfirmSaveDialog tests prove the UI displays whatever data it's handed —
 * they never touch storage. These prove that what actually gets saved is what actually
 * comes back out, which is a separate failure point (a bad column mapping or a wrong
 * WHERE clause wouldn't be caught by a UI test at all).
 *
 * Each test gets a fresh in-memory database, so nothing here is shared or order-dependent.
 */
@RunWith(AndroidJUnit4::class)
class HabitDaoTest {

    private lateinit var database: HabitDatabase
    private lateinit var dao: HabitDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, HabitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.habitDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertedHabit_isReadBackWithTheExactNameAndEmoji() = runTest {
        val habit = HabitEntity(name = "Drink Water", emoji = "💧")
        dao.insertHabit(habit)

        val result = dao.getHabitsWithLogs().first()

        assertEquals(1, result.size)
        assertEquals("Drink Water", result[0].habit.name)
        assertEquals("💧", result[0].habit.emoji)
    }

    @Test
    fun insertedHabit_preservesColorAndFrequencyFields() = runTest {
        val red = 0xFFFF0000.toInt()
        val habit = HabitEntity(
            name = "Stretch",
            emoji = "🤸",
            color = red,
            frequencyType = FrequencyType.SPECIFIC_DAYS,
            customDays = "MONDAY,WEDNESDAY,FRIDAY",
        )
        dao.insertHabit(habit)

        val saved = dao.getHabitsWithLogs().first().first().habit

        assertEquals(red, saved.color)
        assertEquals(FrequencyType.SPECIFIC_DAYS, saved.frequencyType)
        assertEquals("MONDAY,WEDNESDAY,FRIDAY", saved.customDays)
    }

    @Test
    fun updatingAHabit_changesTheNameAndEmojiWithoutCreatingADuplicateRow() = runTest {
        val original = HabitEntity(id = 1, name = "Jog", emoji = "🏃")
        dao.insertHabit(original)

        val edited = original.copy(name = "Jog 30 Minutes", emoji = "🏃‍♂️")
        dao.updateHabit(edited)

        val result = dao.getHabitsWithLogs().first()

        assertEquals(1, result.size)
        assertEquals("Jog 30 Minutes", result[0].habit.name)
        assertEquals("🏃‍♂️", result[0].habit.emoji)
    }

    @Test
    fun archivingAHabit_removesItFromTheMainListButKeepsItInTheArchivedList() = runTest {
        val habit = HabitEntity(id = 1, name = "Old Habit", emoji = "📦")
        dao.insertHabit(habit)

        dao.archiveHabit(habitId = 1)

        val mainList = dao.getHabitsWithLogs().first()
        val archivedList = dao.getArchivedHabitsWithLogs().first()

        assertTrue(mainList.isEmpty())
        assertEquals(1, archivedList.size)
        assertEquals("Old Habit", archivedList[0].habit.name)
    }

    @Test
    fun deletingAHabit_alsoDeletesItsLogs_dueToCascade() = runTest {
        val habit = HabitEntity(id = 1, name = "Read", emoji = "📚")
        dao.insertHabit(habit)
        dao.insertHabitLog(HabitLogsEntity(habitId = 1, date = 1_000L))
        dao.insertHabitLog(HabitLogsEntity(habitId = 1, date = 2_000L))

        // sanity check the logs actually landed before deleting
        val beforeDelete = dao.loadHabitWithLogs(1)
        assertEquals(2, beforeDelete.logs.size)

        dao.deleteHabit(1)

        val remainingHabits = dao.getHabitsWithLogs().first()
        assertTrue(remainingHabits.isEmpty())

        // the habits row being gone doesn't by itself prove the logs were cleaned up —
        // check the habit_logs table directly via the same range query the app uses
        val remainingLogCount = dao.getLogCountInRange(habitId = 1, weekStart = 0L, weekEnd = 10_000L)
        assertEquals(0, remainingLogCount)
    }

    @Test
    fun getLogCountInRange_onlyCountsLogsWithinTheGivenWindow() = runTest {
        val habit = HabitEntity(id = 1, name = "Meditate", emoji = "🧘")
        dao.insertHabit(habit)
        dao.insertHabitLog(HabitLogsEntity(habitId = 1, date = 1_000L))  // inside range
        dao.insertHabitLog(HabitLogsEntity(habitId = 1, date = 2_000L))  // inside range
        dao.insertHabitLog(HabitLogsEntity(habitId = 1, date = 9_999L))  // outside range

        val count = dao.getLogCountInRange(habitId = 1, weekStart = 0L, weekEnd = 5_000L)

        assertEquals(2, count)
    }

    @Test
    fun reorderingHabits_updatesSortOrderToMatchTheGivenSequence() = runTest {
        dao.insertHabit(HabitEntity(id = 1, name = "First", emoji = "1️⃣"))
        dao.insertHabit(HabitEntity(id = 2, name = "Second", emoji = "2️⃣"))
        dao.insertHabit(HabitEntity(id = 3, name = "Third", emoji = "3️⃣"))

        // reverse the order
        dao.updateSortOrders(listOf(3, 2, 1))

        val result = dao.getHabitsWithLogs().first()
        assertEquals(listOf("Third", "Second", "First"), result.map { it.habit.name })
    }

    @Test
    fun aHabitWithNoColorSet_isReadBackWithANullColor() = runTest {
        dao.insertHabit(HabitEntity(name = "No Color Habit", emoji = "⚪"))

        val saved = dao.getHabitsWithLogs().first().first().habit

        assertNull(saved.color)
    }
}

# HabitApp

A native Android habit tracker built with Jetpack Compose, focused on reliable local-first tracking: create habits with flexible schedules, get reminded even after a phone reboot, and see real streaks calculated from actual logged history — not just a day counter.

## Features

- **Create and manage habits** with a name, emoji icon, color tag, and one of three schedule types: daily, specific days of the week, or a target number of times per week
- **Streak tracking** calculated from logged completion dates, correctly handling gaps and "haven't logged today yet" edge cases
- **Scheduled reminders** via `AlarmManager`, rescheduled automatically on device reboot through a `BroadcastReceiver` — reminders survive the phone restarting, not just the app
- **Archive** habits without deleting their history
- **Search and color-filter** habits from the main list
- **Drag-to-reorder** habit list (via `sh.calvin.reorderable`)
- **Runtime notification permission handling** for Android 13+

## Tech stack

| Layer | Choice |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, unidirectional state flow |
| DI | Hilt |
| Persistence | Room |
| Preferences | Jetpack DataStore |
| Async | Kotlin Coroutines + Flow (`StateFlow`, `combine`, `stateIn`) |
| Background work | `AlarmManager` + `BroadcastReceiver` |
| Testing | JUnit 5 (Jupiter), Robolectric, MockK |

State flows one way: Room emits a `Flow`, the repository exposes it through an interface, the ViewModel combines and exposes it as `StateFlow` with `WhileSubscribed(5000)`, and Compose collects it as state. Nothing holds mutable UI state outside the ViewModel.

## Project structure

```
app/src/main/java/android/learn/habitapp/
├── MainActivity.kt              # Activity entry point + nav host wiring only
├── ui/
│   ├── screens/                 # One file per full screen
│   │   ├── HabitMainScreen.kt
│   │   ├── HabitItemScreen.kt
│   │   └── ArchiveScreen.kt
│   └── components/              # Reusable pieces used across screens
│       ├── HabitList.kt
│       ├── DrawerNavigation.kt
│       ├── ColorFilterRow.kt
│       ├── ConfirmSaveDialog.kt
│       ├── EmojiButton.kt
│       ├── IOSInsetListRow.kt
│       └── CommonUi.kt
├── permissions/
│   └── NotificationPermissionHandler.kt
├── notifications/
│   ├── AlarmScheduler.kt
│   ├── BootReciever.kt
│   └── FrequencyEvaluator.kt
├── data/
│   ├── local/                   # Room entities, DAO, database, DataStore prefs
│   └── repository/              # Repository interfaces + implementations
└── StreakCalculator.kt
```

Screens and components are split one-composable-family per file rather than living in a single `MainActivity.kt`, so each piece can be found, reviewed, and tested independently.

## Requirements

- Android Studio (latest stable)
- minSdk 33, targetSdk 36
- JDK 17+

## Running the project

```bash
git clone <repo-url>
cd HabitApp
```

Open in Android Studio, let Gradle sync, then run on a device or emulator running Android 13 (API 33) or higher.

## Testing

```bash
./gradlew test
```

## Known gaps / next steps

- No cloud sync — data is local-only (Room), so it doesn't survive an app uninstall or move devices
- Unit test coverage exists for streak/frequency logic but not yet for ViewModels
- No home screen widget
